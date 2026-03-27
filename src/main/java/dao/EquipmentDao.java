package dao;

import model.Equipment;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EquipmentDao extends BaseDao {
    public List<Equipment> findAll() {
        String sql = "SELECT * FROM equipments ORDER BY equipment_id";
        List<Equipment> equipments = new ArrayList<>();
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql);
                var resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                equipments.add(mapEquipment(resultSet));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach thiet bi.", ex);
        }
        return equipments;
    }

    public Optional<Equipment> findById(int equipmentId) {
        String sql = "SELECT * FROM equipments WHERE equipment_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, equipmentId);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapEquipment(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tim thiet bi.", ex);
        }
        return Optional.empty();
    }

    public boolean create(Equipment equipment) {
        String sql = "INSERT INTO equipments(equipment_name, total_quantity, available_quantity, status) VALUES (?, ?, ?, ?)";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setString(1, equipment.getEquipmentName());
            statement.setInt(2, equipment.getTotalQuantity());
            statement.setInt(3, equipment.getAvailableQuantity());
            statement.setString(4, equipment.getStatus());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tao thiet bi.", ex);
        }
    }

    public boolean update(Equipment equipment) {
        String sql = "UPDATE equipments SET equipment_name = ?, total_quantity = ?, available_quantity = ?, status = ? WHERE equipment_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
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

    public boolean delete(int equipmentId) {
        String sql = "DELETE FROM equipments WHERE equipment_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, equipmentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the xoa thiet bi.", ex);
        }
    }

    public void reserveForBooking(Connection connection, Map<Integer, Integer> equipmentRequests) throws SQLException {
        for (Map.Entry<Integer, Integer> entry : equipmentRequests.entrySet()) {
            int equipmentId = entry.getKey();
            int requiredQty = entry.getValue();

            String lockSql = "SELECT available_quantity, status FROM equipments WHERE equipment_id = ? FOR UPDATE";
            try (var lockStatement = connection.prepareStatement(lockSql)) {
                lockStatement.setInt(1, equipmentId);
                try (var resultSet = lockStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new IllegalArgumentException("Khong tim thay thiet bi id = " + equipmentId);
                    }
                    String status = resultSet.getString("status");
                    int available = resultSet.getInt("available_quantity");
                    if (!"ACTIVE".equalsIgnoreCase(status)) {
                        throw new IllegalArgumentException(
                                "Thiet bi id = " + equipmentId + " khong o trang thai ACTIVE.");
                    }
                    if (requiredQty > available) {
                        throw new IllegalArgumentException(
                                "Thiet bi id = " + equipmentId + " khong du so luong kha dung.");
                    }
                }
            }

            String updateSql = "UPDATE equipments SET available_quantity = available_quantity - ? WHERE equipment_id = ?";
            try (var updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setInt(1, requiredQty);
                updateStatement.setInt(2, equipmentId);
                updateStatement.executeUpdate();
            }
        }
    }

    public void releaseForBooking(Connection connection, Map<Integer, Integer> equipmentRequests) throws SQLException {
        for (Map.Entry<Integer, Integer> entry : equipmentRequests.entrySet()) {
            String updateSql = "UPDATE equipments SET available_quantity = LEAST(total_quantity, available_quantity + ?) WHERE equipment_id = ?";
            try (var statement = connection.prepareStatement(updateSql)) {
                statement.setInt(1, entry.getValue());
                statement.setInt(2, entry.getKey());
                statement.executeUpdate();
            }
        }
    }

    private Equipment mapEquipment(ResultSet resultSet) throws SQLException {
        Equipment equipment = new Equipment();
        equipment.setEquipmentId(resultSet.getInt("equipment_id"));
        equipment.setEquipmentName(resultSet.getString("equipment_name"));
        equipment.setTotalQuantity(resultSet.getInt("total_quantity"));
        equipment.setAvailableQuantity(resultSet.getInt("available_quantity"));
        equipment.setStatus(resultSet.getString("status"));
        return equipment;
    }
}
