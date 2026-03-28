package dao;

import model.Room;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDao extends BaseDao {
    /**
     * Lấy tất cả phòng
     * @return Danh sách tất cả Room
     */
    public List<Room> findAll() {
        String sql = "SELECT * FROM rooms ORDER BY room_id";
        List<Room> rooms = new ArrayList<>();
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql);
                var resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rooms.add(mapRoom(resultSet));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach phong.", ex);
        }
        return rooms;
    }

    /**
     * Tìm phòng theo ID
     * @param roomId ID của phòng cần tìm
     * @return Optional chứa Room nếu tìm thấy, rỗng nếu không
     */
    public Optional<Room> findById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRoom(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tim phong.", ex);
        }
        return Optional.empty();
    }

    /**
     * Tạo phòng mới
     * @param room Thông tin phòng cần tạo
     * @return true nếu tạo thành công, false nếu không
     */
    public boolean create(Room room) {
        String sql = "INSERT INTO rooms(room_name, capacity, location, fixed_equipment, is_active) VALUES (?, ?, ?, ?, ?)";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setString(1, room.getRoomName());
            statement.setInt(2, room.getCapacity());
            statement.setString(3, normalizeBlank(room.getLocation()));
            statement.setString(4, normalizeBlank(room.getFixedEquipment()));
            statement.setBoolean(5, room.isActive());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tao phong.", ex);
        }
    }

    /**
     * Cập nhật thông tin phòng
     * @param room Thông tin phòng cần cập nhật
     * @return true nếu cập nhật thành công, false nếu không
     */
    public boolean update(Room room) {
        String sql = "UPDATE rooms SET room_name = ?, capacity = ?, location = ?, fixed_equipment = ?, is_active = ? WHERE room_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setString(1, room.getRoomName());
            statement.setInt(2, room.getCapacity());
            statement.setString(3, normalizeBlank(room.getLocation()));
            statement.setString(4, normalizeBlank(room.getFixedEquipment()));
            statement.setBoolean(5, room.isActive());
            statement.setInt(6, room.getRoomId());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the cap nhat phong.", ex);
        }
    }

    /**
     * Vô hiệu hóa phòng (soft delete)
     * @param roomId ID của phòng cần vô hiệu hóa
     * @return true nếu vô hiệu hóa thành công, false nếu không
     */
    public boolean deactivate(int roomId) {
        String sql = "UPDATE rooms SET is_active = FALSE WHERE room_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the xoa (deactivate) phong.", ex);
        }
    }

    /**
     * Chuyển đổi ResultSet sang đối tượng Room
     * @param resultSet ResultSet từ database
     * @return Đối tượng Room
     */
    private Room mapRoom(ResultSet resultSet) throws SQLException {
        Room room = new Room();
        room.setRoomId(resultSet.getInt("room_id"));
        room.setRoomName(resultSet.getString("room_name"));
        room.setCapacity(resultSet.getInt("capacity"));
        room.setLocation(resultSet.getString("location"));
        room.setFixedEquipment(resultSet.getString("fixed_equipment"));
        room.setActive(resultSet.getBoolean("is_active"));
        return room;
    }

    /**
     * Chuẩn hóa chuỗi: null nếu rỗng, ngược lại trim
     * @param value Chuỗi cần chuẩn hóa
     * @return Chuỗi đã chuẩn hóa hoặc null
     */
    private String normalizeBlank(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
