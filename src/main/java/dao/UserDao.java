package dao;

import model.User;

import java.util.List;
import java.util.Optional;

/**
 * Interface for User data access operations
 */
public interface UserDao {
    /**
     * Tìm user theo username
     * @param username Username cần tìm
     * @return Optional chứa User nếu tìm thấy, rỗng nếu không
     */
    Optional<User> findByUsername(String username);

    /**
     * Tìm user theo ID
     * @param userId ID của user cần tìm
     * @return Optional chứa User nếu tìm thấy, rỗng nếu không
     */
    Optional<User> findById(int userId);

    /**
     * Tạo user mới
     * @param user Thông tin user cần tạo
     * @return true nếu tạo thành công, false nếu không
     */
    boolean createUser(User user);

    /**
     * Cập nhật thông tin profile của user
     * @param user Thông tin user cần cập nhật
     * @return true nếu cập nhật thành công, false nếu không
     */
    boolean updateProfile(User user);

    /**
     * Lấy danh sách user theo role
     * @param role Role cần tìm (EMPLOYEE, SUPPORT_STAFF, ADMIN)
     * @return Danh sách User có role tương ứng
     */
    List<User> findByRole(String role);

    /**
     * Lấy tất cả user
     * @return Danh sách tất cả User
     */
    List<User> getAllUsers();

    /**
     * Cập nhật thông tin user
     * @param user Thông tin user cần cập nhật
     * @return true nếu cập nhật thành công, false nếu không
     */
    boolean updateUser(User user);

    /**
     * Xóa user
     * @param userId ID của user cần xóa
     * @return true nếu xóa thành công, false nếu không
     */
    boolean deleteUser(int userId);
}
