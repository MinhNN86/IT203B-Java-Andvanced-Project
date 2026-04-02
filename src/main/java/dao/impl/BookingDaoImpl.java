package dao.impl;

import dao.BaseDao;
import dao.BookingDao;
import dao.EquipmentDao;
import model.Booking;
import model.BookingDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of BookingDao interface
 */
public class BookingDaoImpl extends BaseDao implements BookingDao {
    private final EquipmentDao equipmentDao = new EquipmentDaoImpl();

    /**
     * Tạo booking mới với trạng thái PENDING và chi tiết thiết bị/dịch vụ
     * 
     * @param booking           Thông tin booking cơ bản
     * @param equipmentRequests Map của equipment_id -> quantity
     * @param serviceRequests   Map của service_id -> quantity
     * @return ID của booking vừa tạo
     */
    @Override
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

                String bookingSql = "INSERT INTO bookings(employee_id, room_id, start_time, end_time, booking_status, prep_status) VALUES (?, ?, ?, ?, 'PENDING', 'PENDING')";

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

    /**
     * Tìm booking theo ID
     * 
     * @param bookingId ID của booking cần tìm
     * @return Optional chứa Booking nếu tìm thấy, rỗng nếu không
     */
    @Override
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

    /**
     * Lấy danh sách booking đang chờ duyệt
     *
     * @return Danh sách BookingDetail có trạng thái PENDING
     */
    @Override
    public List<BookingDetail> findPendingBookings() {
        String sql = "SELECT b.booking_id, " +
                "       r.room_name, " +
                "       u.full_name AS employee_name, " +
                "       b.start_time, " +
                "       b.end_time, " +
                "       b.booking_status, " +
                "       b.prep_status, " +
                "       COALESCE(ss.full_name, '-') AS support_staff_name " +
                "FROM bookings b " +
                "JOIN rooms r ON r.room_id = b.room_id " +
                "JOIN users u ON u.user_id = b.employee_id " +
                "LEFT JOIN users ss ON ss.user_id = b.support_staff_id " +
                "WHERE b.booking_status = 'PENDING' " +
                "ORDER BY b.start_time DESC";

        List<BookingDetail> details = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                details.add(mapBookingDetail(resultSet));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach booking dang cho duyet.", ex);
        }
        return details;
    }

    /**
     * Lấy tất cả booking
     *
     * @return Danh sách tất cả BookingDetail
     */
    @Override
    public List<BookingDetail> findAllBookings() {
        String sql = "SELECT b.booking_id, " +
                "       r.room_name, " +
                "       u.full_name AS employee_name, " +
                "       b.start_time, " +
                "       b.end_time, " +
                "       b.booking_status, " +
                "       b.prep_status, " +
                "       COALESCE(ss.full_name, '-') AS support_staff_name " +
                "FROM bookings b " +
                "JOIN rooms r ON r.room_id = b.room_id " +
                "JOIN users u ON u.user_id = b.employee_id " +
                "LEFT JOIN users ss ON ss.user_id = b.support_staff_id " +
                "ORDER BY b.start_time DESC";

        List<BookingDetail> details = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                details.add(mapBookingDetail(resultSet));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach booking.", ex);
        }
        return details;
    }

    /**
     * Lấy danh sách booking của một nhân viên
     *
     * @param employeeId ID của nhân viên
     * @return Danh sách BookingDetail của nhân viên
     */
    @Override
    public List<BookingDetail> findByEmployee(int employeeId) {
        String sql = "SELECT b.booking_id, " +
                "       r.room_name, " +
                "       u.full_name AS employee_name, " +
                "       b.start_time, " +
                "       b.end_time, " +
                "       b.booking_status, " +
                "       b.prep_status, " +
                "       COALESCE(ss.full_name, '-') AS support_staff_name " +
                "FROM bookings b " +
                "JOIN rooms r ON r.room_id = b.room_id " +
                "JOIN users u ON u.user_id = b.employee_id " +
                "LEFT JOIN users ss ON ss.user_id = b.support_staff_id " +
                "WHERE b.employee_id = ? " +
                "ORDER BY b.start_time DESC";

        List<BookingDetail> details = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    details.add(mapBookingDetail(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach booking cua nhan vien.", ex);
        }
        return details;
    }

    /**
     * Lấy danh sách booking được giao cho một support staff
     *
     * @param supportStaffId ID của support staff
     * @return Danh sách BookingDetail được giao cho support staff
     */
    @Override
    public List<BookingDetail> findBySupportStaff(int supportStaffId) {
        String sql = "SELECT b.booking_id, " +
                "       r.room_name, " +
                "       u.full_name AS employee_name, " +
                "       b.start_time, " +
                "       b.end_time, " +
                "       b.booking_status, " +
                "       b.prep_status, " +
                "       COALESCE(ss.full_name, '-') AS support_staff_name " +
                "FROM bookings b " +
                "JOIN rooms r ON r.room_id = b.room_id " +
                "JOIN users u ON u.user_id = b.employee_id " +
                "LEFT JOIN users ss ON ss.user_id = b.support_staff_id " +
                "WHERE b.support_staff_id = ? " +
                "ORDER BY b.start_time DESC";

        List<BookingDetail> details = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supportStaffId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    details.add(mapBookingDetail(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach booking duoc giao cho support staff.", ex);
        }
        return details;
    }

    /**
     * Duyệt booking đang chờ
     * 
     * @param bookingId      ID của booking cần duyệt
     * @param supportStaffId ID của support staff được giao
     * @return true nếu duyệt thành công, false nếu không
     */
    @Override
    public boolean approvePendingBooking(int bookingId, int supportStaffId) {
        String sql = "UPDATE bookings SET booking_status = 'APPROVED', support_staff_id = ?, prep_status = 'PREPARING' WHERE booking_id = ? AND booking_status = 'PENDING'";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supportStaffId);
            statement.setInt(2, bookingId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the duyet booking.", ex);
        }
    }

    /**
     * Từ chối booking đang chờ và giải phóng thiết bị
     * 
     * @param bookingId ID của booking cần từ chối
     * @return true nếu từ chối thành công
     */
    @Override
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

    /**
     * Hủy booking đang chờ bởi nhân viên
     * 
     * @param bookingId  ID của booking cần hủy
     * @param employeeId ID của nhân viên yêu cầu hủy
     * @return true nếu hủy thành công
     */
    @Override
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

    /**
     * Cập nhật trạng thái chuẩn bị của booking
     * 
     * @param bookingId      ID của booking
     * @param supportStaffId ID của support staff
     * @param prepStatus     Trạng thái mới (PREPARING, READY, COMPLETED)
     * @return true nếu cập nhật thành công
     */
    @Override
    public boolean updatePreparationStatus(int bookingId, int supportStaffId, String prepStatus) {
        String sql = "UPDATE bookings SET prep_status = ? WHERE booking_id = ? AND support_staff_id = ? AND booking_status = 'APPROVED'";
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

    /**
     * Khóa booking để kiểm tra trạng thái PENDING
     * 
     * @param connection Kết nối database
     * @param bookingId  ID của booking
     * @return Booking đã khóa
     */
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

    /**
     * Lấy danh sách thiết bị đã đặt cho booking
     * 
     * @param connection Kết nối database
     * @param bookingId  ID của booking
     * @return Map của equipment_id -> quantity
     */
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

    /**
     * Thêm chi tiết thiết bị cho booking
     * 
     * @param connection        Kết nối database
     * @param bookingId         ID của booking
     * @param equipmentRequests Map của equipment_id -> quantity
     */
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

    /**
     * Thêm chi tiết dịch vụ cho booking
     * 
     * @param connection      Kết nối database
     * @param bookingId       ID của booking
     * @param serviceRequests Map của service_id -> quantity
     */
    private void insertBookingServices(Connection connection, int bookingId, Map<Integer, Integer> serviceRequests)
            throws SQLException {
        if (serviceRequests.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO booking_services(booking_id, service_id, quantity, price_at_booking) SELECT ?, s.service_id, ?, s.price FROM services s WHERE s.service_id = ?";

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

    /**
     * Kiểm tra xem phòng có bị trùng lịch không
     * 
     * @param connection       Kết nối database
     * @param roomId           ID của phòng
     * @param startTime        Thời gian bắt đầu
     * @param endTime          Thời gian kết thúc
     * @param excludeBookingId ID của booking cần loại khỏi kiểm tra (null nếu
     *                         không)
     * @return true nếu có trùng lịch, false nếu không
     */
    private boolean hasRoomConflict(Connection connection, int roomId, LocalDateTime startTime, LocalDateTime endTime,
            Integer excludeBookingId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_id = ? AND booking_status IN ('PENDING', 'APPROVED') AND (? < end_time AND ? > start_time) AND (? IS NULL OR booking_id <> ?)";

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

    /**
     * Chuyển đổi ResultSet sang đối tượng Booking
     * 
     * @param resultSet ResultSet từ database
     * @return Đối tượng Booking
     */
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

    /**
     * Chuyển đổi ResultSet sang đối tượng BookingDetail
     * 
     * @param resultSet ResultSet từ database
     * @return Đối tượng BookingDetail
     */
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
        return detail;
    }

    /**
     * Lấy danh sách phòng trống trong khoảng thời gian
     * 
     * @param startTime Thời gian bắt đầu
     * @param endTime   Thời gian kết thúc
     * @return Danh sách ID phòng trống
     */
    @Override
    public List<Integer> findAvailableRoomIds(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT r.room_id FROM rooms r WHERE r.is_active = 1 AND r.room_id NOT IN (SELECT b.room_id FROM bookings b WHERE b.booking_status IN ('PENDING', 'APPROVED') AND (? < b.end_time AND ? > b.start_time))";
        List<Integer> availableRoomIds = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(startTime));
            statement.setTimestamp(2, Timestamp.valueOf(endTime));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    availableRoomIds.add(resultSet.getInt("room_id"));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong tim danh sach phong trong.", ex);
        }
        return availableRoomIds;
    }
}
