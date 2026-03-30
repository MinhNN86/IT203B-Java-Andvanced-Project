package dao;

import model.Service;

import java.util.List;
import java.util.Optional;

/**
 * Interface for Service data access operations
 */
public interface ServiceDao {
    /**
     * Lấy tất cả dịch vụ
     * @return Danh sách tất cả Service
     */
    List<Service> findAll();

    /**
     * Tìm dịch vụ theo ID
     * @param serviceId ID của dịch vụ cần tìm
     * @return Optional chứa Service nếu tìm thấy, rỗng nếu không
     */
    Optional<Service> findById(int serviceId);

    /**
     * Tạo dịch vụ mới
     * @param service Thông tin dịch vụ cần tạo
     * @return true nếu tạo thành công, false nếu không
     */
    boolean create(Service service);

    /**
     * Cập nhật thông tin dịch vụ
     * @param service Thông tin dịch vụ cần cập nhật
     * @return true nếu cập nhật thành công, false nếu không
     */
    boolean update(Service service);

    /**
     * Xóa dịch vụ
     * @param serviceId ID của dịch vụ cần xóa
     * @return true nếu xóa thành công, false nếu không
     */
    boolean delete(int serviceId);
}
