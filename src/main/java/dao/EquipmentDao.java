package dao;

import model.Equipment;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for Equipment data access operations
 */
public interface EquipmentDao {
    /**
     * Lấy tất cả thiết bị
     * @return Danh sách tất cả Equipment
     */
    List<Equipment> findAll();

    /**
     * Tìm thiết bị theo ID
     * @param equipmentId ID của thiết bị cần tìm
     * @return Optional chứa Equipment nếu tìm thấy, rỗng nếu không
     */
    Optional<Equipment> findById(int equipmentId);

    /**
     * Tạo thiết bị mới
     * @param equipment Thông tin thiết bị cần tạo
     * @return true nếu tạo thành công, false nếu không
     */
    boolean create(Equipment equipment);

    /**
     * Cập nhật thông tin thiết bị
     * @param equipment Thông tin thiết bị cần cập nhật
     * @return true nếu cập nhật thành công, false nếu không
     */
    boolean update(Equipment equipment);

    /**
     * Xóa thiết bị
     * @param equipmentId ID của thiết bị cần xóa
     * @return true nếu xóa thành công, false nếu không
     */
    boolean delete(int equipmentId);

    /**
     * Giữ chỗ thiết bị cho booking (trong transaction)
     * @param connection Kết nối database
     * @param equipmentRequests Map của equipment_id -> quantity cần đặt
     * @throws SQLException nếu có lỗi database
     * @throws IllegalArgumentException nếu thiết bị không tồn tại, không ACTIVE, hoặc không đủ số lượng
     */
    void reserveForBooking(Connection connection, Map<Integer, Integer> equipmentRequests) throws SQLException;

    /**
     * Giải phóng thiết bị khi booking bị hủy/từ chối (trong transaction)
     * @param connection Kết nối database
     * @param equipmentRequests Map của equipment_id -> quantity cần giải phóng
     * @throws SQLException nếu có lỗi database
     */
    void releaseForBooking(Connection connection, Map<Integer, Integer> equipmentRequests) throws SQLException;
}
