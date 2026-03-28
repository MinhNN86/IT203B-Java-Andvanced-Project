package dao;

import model.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceDao extends BaseDao {
    /**
     * Lấy tất cả dịch vụ
     * @return Danh sách tất cả Service
     */
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

    /**
     * Tìm dịch vụ theo ID
     * @param serviceId ID của dịch vụ cần tìm
     * @return Optional chứa Service nếu tìm thấy, rỗng nếu không
     */
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

    /**
     * Tạo dịch vụ mới
     * @param service Thông tin dịch vụ cần tạo
     * @return true nếu tạo thành công, false nếu không
     */
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

    /**
     * Cập nhật thông tin dịch vụ
     * @param service Thông tin dịch vụ cần cập nhật
     * @return true nếu cập nhật thành công, false nếu không
     */
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

    /**
     * Xóa dịch vụ
     * @param serviceId ID của dịch vụ cần xóa
     * @return true nếu xóa thành công, false nếu không
     */
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

    /**
     * Chuyển đổi ResultSet sang đối tượng Service
     * @param resultSet ResultSet từ database
     * @return Đối tượng Service
     */
    private Service mapService(ResultSet resultSet) throws SQLException {
        Service service = new Service();
        service.setServiceId(resultSet.getInt("service_id"));
        service.setServiceName(resultSet.getString("service_name"));
        service.setPrice(resultSet.getDouble("price"));
        service.setUnit(resultSet.getString("unit"));
        return service;
    }

    /**
     * Chuẩn hóa chuỗi: null nếu rỗng, ngược lại trim
     * @param value Chuỗi cần chuẩn hóa
     * @return Chuỗi đã chuẩn hóa hoặc null
     */
    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
