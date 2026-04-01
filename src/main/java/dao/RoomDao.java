package dao;

import model.Room;

import java.util.List;
import java.util.Optional;

/**
 * Interface for Room data access operations
 */
public interface RoomDao {
    /**
     * Lấy tất cả phòng
     * @return Danh sách tất cả Room
     */
    List<Room> findAll();

    /**
     * Tim phong theo ten (tim kiem tuong doi)
     * @param keyword Tu khoa ten phong
     * @return Danh sach phong khop voi tu khoa
     */
    List<Room> searchByName(String keyword);

    /**
     * Tìm phòng theo ID
     * @param roomId ID của phòng cần tìm
     * @return Optional chứa Room nếu tìm thấy, rỗng nếu không
     */
    Optional<Room> findById(int roomId);

    /**
     * Tạo phòng mới
     * @param room Thông tin phòng cần tạo
     * @return true nếu tạo thành công, false nếu không
     */
    boolean create(Room room);

    /**
     * Cập nhật thông tin phòng
     * @param room Thông tin phòng cần cập nhật
     * @return true nếu cập nhật thành công, false nếu không
     */
    boolean update(Room room);

    /**
     * Vô hiệu hóa phòng (soft delete)
     * @param roomId ID của phòng cần vô hiệu hóa
     * @return true nếu vô hiệu hóa thành công, false nếu không
     */
    boolean deactivate(int roomId);
}
