package dao;

import model.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceDao extends BaseDao {
    public List<Service> findAll() {
        String sql = "SELECT * FROM services ORDER BY service_id";
        List<Service> services = new ArrayList<>();
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql);
                var resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                services.add(mapService(resultSet));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach dich vu.", ex);
        }
        return services;
    }

    public Optional<Service> findById(int serviceId) {
        String sql = "SELECT * FROM services WHERE service_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceId);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapService(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tim dich vu.", ex);
        }
        return Optional.empty();
    }

    public boolean create(Service service) {
        String sql = "INSERT INTO services(service_name, price, unit) VALUES (?, ?, ?)";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setString(1, service.getServiceName());
            statement.setDouble(2, service.getPrice());
            statement.setString(3, normalizeBlank(service.getUnit()));
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tao dich vu.", ex);
        }
    }

    public boolean update(Service service) {
        String sql = "UPDATE services SET service_name = ?, price = ?, unit = ? WHERE service_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setString(1, service.getServiceName());
            statement.setDouble(2, service.getPrice());
            statement.setString(3, normalizeBlank(service.getUnit()));
            statement.setInt(4, service.getServiceId());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the cap nhat dich vu.", ex);
        }
    }

    public boolean delete(int serviceId) {
        String sql = "DELETE FROM services WHERE service_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the xoa dich vu.", ex);
        }
    }

    private Service mapService(ResultSet resultSet) throws SQLException {
        Service service = new Service();
        service.setServiceId(resultSet.getInt("service_id"));
        service.setServiceName(resultSet.getString("service_name"));
        service.setPrice(resultSet.getDouble("price"));
        service.setUnit(resultSet.getString("unit"));
        return service;
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
