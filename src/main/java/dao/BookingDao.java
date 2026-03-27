package dao;

import model.Booking;
import model.BookingDetail;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BookingDao extends BaseDao {
    private final EquipmentDao equipmentDao = new EquipmentDao();

    public int createPendingBookingWithDetails(Booking booking,
            Map<Integer, Integer> equipmentRequests,
            Map<Integer, Integer> serviceRequests) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (hasRoomConflict(connection, booking.getRoomId(), booking.getStartTime(), booking.getEndTime(),
                        null)) {
                    throw new IllegalArgumentException("Phong da co lich trung trong khung gio nay.");
                }

                String bookingSql = """
                        INSERT INTO bookings(employee_id, room_id, start_time, end_time, booking_status, prep_status)
                        VALUES (?, ?, ?, ?, 'PENDING', 'PENDING')
                        """;

                int bookingId;
                try (PreparedStatement statement = connection.prepareStatement(bookingSql,
                        Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, booking.getEmployeeId());
                    statement.setInt(2, booking.getRoomId());
                    statement.setTimestamp(3, Timestamp.valueOf(booking.getStartTime()));
                    statement.setTimestamp(4, Timestamp.valueOf(booking.getEndTime()));
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Khong tao duoc booking.");
                        }
                        bookingId = keys.getInt(1);
                    }
                }

                equipmentDao.reserveForBooking(connection, equipmentRequests);
                insertBookingEquipments(connection, bookingId, equipmentRequests);
                insertBookingServices(connection, bookingId, serviceRequests);

                connection.commit();
                return bookingId;
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tao booking.", ex);
        }
    }

    public Optional<Booking> findById(int bookingId) {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapBooking(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tim booking.", ex);
        }
        return Optional.empty();
    }

    public List<BookingDetail> findPendingBookings() {
        String condition = "WHERE b.booking_status = 'PENDING'";
        return findBookingDetails(condition);
    }

    public List<BookingDetail> findAllBookings() {
        return findBookingDetails("");
    }

    public List<BookingDetail> findByEmployee(int employeeId) {
        String condition = "WHERE b.employee_id = " + employeeId;
        return findBookingDetails(condition);
    }

    public List<BookingDetail> findBySupportStaff(int supportStaffId) {
        String condition = "WHERE b.support_staff_id = " + supportStaffId;
        return findBookingDetails(condition);
    }

    public boolean approvePendingBooking(int bookingId, int supportStaffId) {
        String sql = """
                UPDATE bookings
                SET booking_status = 'APPROVED', support_staff_id = ?, prep_status = 'PREPARING'
                WHERE booking_id = ? AND booking_status = 'PENDING'
                """;
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supportStaffId);
            statement.setInt(2, bookingId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the duyet booking.", ex);
        }
    }

    public boolean rejectPendingBooking(int bookingId) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking booking = lockPendingBooking(connection, bookingId);
                Map<Integer, Integer> equipmentRequests = getEquipmentRequests(connection, booking.getBookingId());
                equipmentDao.releaseForBooking(connection, equipmentRequests);

                String rejectSql = "UPDATE bookings SET booking_status = 'REJECTED' WHERE booking_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(rejectSql)) {
                    statement.setInt(1, bookingId);
                    statement.executeUpdate();
                }

                connection.commit();
                return true;
            } catch (Exception ex) {
                connection.rollback();
                if (ex instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) ex;
                }
                throw new RuntimeException("Khong the tu choi booking.", ex);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tu choi booking.", ex);
        }
    }

    public boolean cancelPendingBooking(int bookingId, int employeeId) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                String lockSql = "SELECT * FROM bookings WHERE booking_id = ? AND employee_id = ? FOR UPDATE";
                Booking booking;
                try (PreparedStatement lockStmt = connection.prepareStatement(lockSql)) {
                    lockStmt.setInt(1, bookingId);
                    lockStmt.setInt(2, employeeId);
                    try (ResultSet resultSet = lockStmt.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new IllegalArgumentException("Khong tim thay booking cua ban voi id nay.");
                        }
                        booking = mapBooking(resultSet);
                    }
                }

                if (!"PENDING".equals(booking.getBookingStatus())) {
                    throw new IllegalArgumentException("Chi booking PENDING moi duoc huy.");
                }

                Map<Integer, Integer> equipmentRequests = getEquipmentRequests(connection, bookingId);
                equipmentDao.releaseForBooking(connection, equipmentRequests);

                String cancelSql = "UPDATE bookings SET booking_status = 'CANCELLED' WHERE booking_id = ?";
                try (PreparedStatement cancelStmt = connection.prepareStatement(cancelSql)) {
                    cancelStmt.setInt(1, bookingId);
                    cancelStmt.executeUpdate();
                }

                connection.commit();
                return true;
            } catch (Exception ex) {
                connection.rollback();
                if (ex instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) ex;
                }
                throw new RuntimeException("Khong the huy booking.", ex);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the huy booking.", ex);
        }
    }

    public boolean updatePreparationStatus(int bookingId, int supportStaffId, String prepStatus) {
        String sql = """
                UPDATE bookings
                SET prep_status = ?
                WHERE booking_id = ?
                  AND support_staff_id = ?
                  AND booking_status = 'APPROVED'
                """;
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prepStatus);
            statement.setInt(2, bookingId);
            statement.setInt(3, supportStaffId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the cap nhat trang thai chuan bi.", ex);
        }
    }

    private Booking lockPendingBooking(Connection connection, int bookingId) throws SQLException {
        String lockSql = "SELECT * FROM bookings WHERE booking_id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(lockSql)) {
            statement.setInt(1, bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Khong tim thay booking.");
                }
                Booking booking = mapBooking(resultSet);
                if (!"PENDING".equals(booking.getBookingStatus())) {
                    throw new IllegalArgumentException("Booking khong o trang thai PENDING.");
                }
                return booking;
            }
        }
    }

    private Map<Integer, Integer> getEquipmentRequests(Connection connection, int bookingId) throws SQLException {
        String sql = "SELECT equipment_id, quantity FROM booking_equipments WHERE booking_id = ?";
        Map<Integer, Integer> requests = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.put(resultSet.getInt("equipment_id"), resultSet.getInt("quantity"));
                }
            }
        }
        return requests;
    }

    private void insertBookingEquipments(Connection connection, int bookingId, Map<Integer, Integer> equipmentRequests)
            throws SQLException {
        if (equipmentRequests.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO booking_equipments(booking_id, equipment_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<Integer, Integer> entry : equipmentRequests.entrySet()) {
                statement.setInt(1, bookingId);
                statement.setInt(2, entry.getKey());
                statement.setInt(3, entry.getValue());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertBookingServices(Connection connection, int bookingId, Map<Integer, Integer> serviceRequests)
            throws SQLException {
        if (serviceRequests.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO booking_services(booking_id, service_id, quantity, price_at_booking)
                SELECT ?, s.service_id, ?, s.price
                FROM services s
                WHERE s.service_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<Integer, Integer> entry : serviceRequests.entrySet()) {
                statement.setInt(1, bookingId);
                statement.setInt(2, entry.getValue());
                statement.setInt(3, entry.getKey());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private boolean hasRoomConflict(Connection connection,
            int roomId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer excludeBookingId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM bookings
                WHERE room_id = ?
                  AND booking_status IN ('PENDING', 'APPROVED')
                  AND (? < end_time AND ? > start_time)
                  AND (? IS NULL OR booking_id <> ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            statement.setTimestamp(2, Timestamp.valueOf(startTime));
            statement.setTimestamp(3, Timestamp.valueOf(endTime));
            if (excludeBookingId == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
                statement.setNull(5, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, excludeBookingId);
                statement.setInt(5, excludeBookingId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    private List<BookingDetail> findBookingDetails(String whereClause) {
        String sql = """
                SELECT b.booking_id,
                	   r.room_name,
                	   u.full_name AS employee_name,
                	   b.start_time,
                	   b.end_time,
                	   b.booking_status,
                	   b.prep_status,
                	   COALESCE(ss.full_name, '-') AS support_staff_name,
                	   COALESCE(eq.equipment_summary, '-') AS equipment_summary,
                	   COALESCE(sv.service_summary, '-') AS service_summary
                FROM bookings b
                JOIN rooms r ON r.room_id = b.room_id
                JOIN users u ON u.user_id = b.employee_id
                LEFT JOIN users ss ON ss.user_id = b.support_staff_id
                LEFT JOIN (
                	SELECT be.booking_id,
                		   GROUP_CONCAT(CONCAT(e.equipment_name, ' x', be.quantity) ORDER BY e.equipment_name SEPARATOR ', ') AS equipment_summary
                	FROM booking_equipments be
                	JOIN equipments e ON e.equipment_id = be.equipment_id
                	GROUP BY be.booking_id
                ) eq ON eq.booking_id = b.booking_id
                LEFT JOIN (
                	SELECT bs.booking_id,
                		   GROUP_CONCAT(CONCAT(s.service_name, ' x', bs.quantity) ORDER BY s.service_name SEPARATOR ', ') AS service_summary
                	FROM booking_services bs
                	JOIN services s ON s.service_id = bs.service_id
                	GROUP BY bs.booking_id
                ) sv ON sv.booking_id = b.booking_id
                %s
                ORDER BY b.start_time DESC
                """
                .formatted(whereClause);

        List<BookingDetail> details = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                details.add(mapBookingDetail(resultSet));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach booking chi tiet.", ex);
        }
        return details;
    }

    private Booking mapBooking(ResultSet resultSet) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(resultSet.getInt("booking_id"));
        booking.setEmployeeId(resultSet.getInt("employee_id"));
        booking.setRoomId(resultSet.getInt("room_id"));
        booking.setStartTime(resultSet.getTimestamp("start_time").toLocalDateTime());
        booking.setEndTime(resultSet.getTimestamp("end_time").toLocalDateTime());
        booking.setBookingStatus(resultSet.getString("booking_status"));

        int supportId = resultSet.getInt("support_staff_id");
        if (!resultSet.wasNull()) {
            booking.setSupportStaffId(supportId);
        }

        booking.setPrepStatus(resultSet.getString("prep_status"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            booking.setCreatedAt(createdAt.toLocalDateTime());
        }
        return booking;
    }

    private BookingDetail mapBookingDetail(ResultSet resultSet) throws SQLException {
        BookingDetail detail = new BookingDetail();
        detail.setBookingId(resultSet.getInt("booking_id"));
        detail.setRoomName(resultSet.getString("room_name"));
        detail.setEmployeeName(resultSet.getString("employee_name"));
        detail.setStartTime(resultSet.getTimestamp("start_time").toLocalDateTime());
        detail.setEndTime(resultSet.getTimestamp("end_time").toLocalDateTime());
        detail.setBookingStatus(resultSet.getString("booking_status"));
        detail.setPrepStatus(resultSet.getString("prep_status"));
        detail.setSupportStaffName(resultSet.getString("support_staff_name"));
        detail.setEquipmentSummary(resultSet.getString("equipment_summary"));
        detail.setServiceSummary(resultSet.getString("service_summary"));
        return detail;
    }
}
