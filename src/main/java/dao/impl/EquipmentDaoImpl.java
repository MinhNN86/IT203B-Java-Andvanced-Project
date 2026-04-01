package dao.impl;

import dao.BaseDao;
import dao.EquipmentDao;
import model.Equipment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of EquipmentDao interface
 */
public class EquipmentDaoImpl extends BaseDao implements EquipmentDao {
    /**
     * Lấy tất cả thiết bị
     * @return Danh sách tất cả Equipment
     */
    @Override
    public List<Equipment> findAll() {
        String sql = "SELECT * FROM equipments ORDER BY equipment_id";
        List<Equipment> equipments = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                equipments.add(mapEquipment(resultSet));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach thiet bi.", ex);
        }
        return equipments;
    }

    /**
     * Tìm thiết bị theo ID
     * @param equipmentId ID của thiết bị cần tìm
     * @return Optional chứa Equipment nếu tìm thấy, rỗng nếu không
     */
    @Override
    public Optional<Equipment> findById(int equipmentId) {
        String sql = "SELECT * FROM equipments WHERE equipment_id = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, equipmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapEquipment(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tim thiet bi.", ex);
        }
        return Optional.empty();
    }

    /**
     * Tạo thiết bị mới
     * @param equipment Thông tin thiết bị cần tạo
     * @return true nếu tạo thành công, false nếu không
     */
    @Override
    public boolean create(Equipment equipment) {
        String sql = "INSERT INTO equipments(equipment_name, total_quantity, available_quantity, status) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, equipment.getEquipmentName());
            statement.setInt(2, equipment.getTotalQuantity());
            statement.setInt(3, equipment.getAvailableQuantity());
            statement.setString(4, equipment.getStatus());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tao thiet bi.", ex);
        }
    }

    /**
     * Cập nhật thông tin thiết bị
     * @param equipment Thông tin thiết bị cần cập nhật
     * @return true nếu cập nhật thành công, false nếu không
     */
    @Override
    public boolean update(Equipment equipment) {
        String sql = "UPDATE equipments SET equipment_name = ?, total_quantity = ?, available_quantity = ?, status = ? WHERE equipment_id = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, equipment.getEquipmentName());
            statement.setInt(2, equipment.getTotalQuantity());
            statement.setInt(3, equipment.getAvailableQuantity());
            statement.setString(4, equipment.getStatus());
            statement.setInt(5, equipment.getEquipmentId());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the cap nhat thiet bi.", ex);
        }
    }

    /**
     * Xóa thiết bị
     * @param equipmentId ID của thiết bị cần xóa
     * @return true nếu xóa thành công, false nếu không
     */
    @Override
    public boolean delete(int equipmentId) {
        String sql = "DELETE FROM equipments WHERE equipment_id = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, equipmentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the xoa thiet bi.", ex);
        }
    }

    /**
     * Giữ chỗ thiết bị cho booking (trong transaction)
     * @param connection Kết nối database
     * @param equipmentRequests Map của equipment_id -> quantity cần đặt
     * @throws SQLException nếu có lỗi database
     * @throws IllegalArgumentException nếu thiết bị không tồn tại, không ACTIVE, hoặc không đủ số lượng
     */
    @Override
    public void reserveForBooking(Connection connection, Map<Integer, Integer> equipmentRequests) throws SQLException {
        for (Map.Entry<Integer, Integer> entry : equipmentRequests.entrySet()) {
            int equipmentId = entry.getKey();
            int requiredQty = entry.getValue();

            String lockSql = "SELECT available_quantity, status FROM equipments WHERE equipment_id = ? FOR UPDATE";
            try (PreparedStatement lockStatement = connection.prepareStatement(lockSql)) {
                lockStatement.setInt(1, equipmentId);
                try (ResultSet resultSet = lockStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new IllegalArgumentException("Khong tim thay thiet bi id = " + equipmentId);
                    }
                    String status = resultSet.getString("status");
                    int available = resultSet.getInt("available_quantity");
                    if (!"ACTIVE".equalsIgnoreCase(status)) {
                        throw new IllegalArgumentException("Thiet bi id = " + equipmentId + " khong o trang thai ACTIVE.");
                    }
                    if (requiredQty > available) {
                        throw new IllegalArgumentException("Thiet bi id = " + equipmentId + " khong du so luong kha dung.");
                    }
                }
            }

            String updateSql = "UPDATE equipments SET available_quantity = available_quantity - ? WHERE equipment_id = ?";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setInt(1, requiredQty);
                updateStatement.setInt(2, equipmentId);
                updateStatement.executeUpdate();
            }
        }
    }

    /**
     * Giải phóng thiết bị khi booking bị hủy/từ chối (trong transaction)
     * @param connection Kết nối database
     * @param equipmentRequests Map của equipment_id -> quantity cần giải phóng
     * @throws SQLException nếu có lỗi database
     */
    @Override
    public void releaseForBooking(Connection connection, Map<Integer, Integer> equipmentRequests) throws SQLException {
        for (Map.Entry<Integer, Integer> entry : equipmentRequests.entrySet()) {
            String updateSql = "UPDATE equipments SET available_quantity = LEAST(total_quantity, available_quantity + ?) WHERE equipment_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                statement.setInt(1, entry.getValue());
                statement.setInt(2, entry.getKey());
                statement.executeUpdate();
            }
        }
    }

    /**
     * Chuyển đổi ResultSet sang đối tượng Equipment
     * @param resultSet ResultSet từ database
     * @return Đối tượng Equipment
     */
    private Equipment mapEquipment(ResultSet resultSet) throws SQLException {
        Equipment equipment = new Equipment();
        equipment.setEquipmentId(resultSet.getInt("equipment_id"));
        equipment.setEquipmentName(resultSet.getString("equipment_name"));
        equipment.setTotalQuantity(resultSet.getInt("total_quantity"));
        equipment.setAvailableQuantity(resultSet.getInt("available_quantity"));
        equipment.setStatus(resultSet.getString("status"));
        return equipment;
    }

    /**
     * Kiểm tra xem tên thiết bị đã tồn tại chưa
     * @param equipmentName Tên thiết bị cần kiểm tra
     * @return true nếu tên đã tồn tại, false nếu chưa
     */
    @Override
    public boolean existsByName(String equipmentName) {
        String sql = "SELECT COUNT(*) FROM equipments WHERE equipment_name = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, equipmentName.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the kiem tra ten thiet bi.", ex);
        }
        return false;
    }
}
