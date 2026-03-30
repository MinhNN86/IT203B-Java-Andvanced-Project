package dao.impl;

import dao.BaseDao;
import dao.UserDao;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserDao interface
 */
public class UserDaoImpl extends BaseDao implements UserDao {
    /**
     * Tìm user theo username
     * @param username Username cần tìm
     * @return Optional chứa User nếu tìm thấy, rỗng nếu không
     */
    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tim user theo username.", ex);
        }
        return Optional.empty();
    }

    /**
     * Tìm user theo ID
     * @param userId ID của user cần tìm
     * @return Optional chứa User nếu tìm thấy, rỗng nếu không
     */
    @Override
    public Optional<User> findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tim user theo id.", ex);
        }
        return Optional.empty();
    }

    /**
     * Tạo user mới
     * @param user Thông tin user cần tạo
     * @return true nếu tạo thành công, false nếu không
     */
    @Override
    public boolean createUser(User user) {
        String sql = "INSERT INTO users(username, password, role, full_name, email, phone, department) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getRole());
            statement.setString(4, user.getFullName());
            statement.setString(5, normalizeBlank(user.getEmail()));
            statement.setString(6, normalizeBlank(user.getPhone()));
            statement.setString(7, normalizeBlank(user.getDepartment()));

            int affected = statement.executeUpdate();
            if (affected == 0) {
                return false;
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                }
            }
            return true;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tao user moi.", ex);
        }
    }

    /**
     * Cập nhật thông tin profile của user
     * @param user Thông tin user cần cập nhật
     * @return true nếu cập nhật thành công, false nếu không
     */
    @Override
    public boolean updateProfile(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, department = ? WHERE user_id = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getFullName());
            statement.setString(2, normalizeBlank(user.getEmail()));
            statement.setString(3, normalizeBlank(user.getPhone()));
            statement.setString(4, normalizeBlank(user.getDepartment()));
            statement.setInt(5, user.getUserId());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the cap nhat profile.", ex);
        }
    }

    /**
     * Lấy danh sách user theo role
     * @param role Role cần tìm (EMPLOYEE, SUPPORT_STAFF, ADMIN)
     * @return Danh sách User có role tương ứng
     */
    @Override
    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY full_name";
        List<User> users = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach user theo role.", ex);
        }
        return users;
    }

    /**
     * Lấy tất cả user
     * @return Danh sách tất cả User
     */
    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY full_name";
        List<User> users = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach tat ca user.", ex);
        }
        return users;
    }

    /**
     * Cập nhật thông tin user
     * @param user Thông tin user cần cập nhật
     * @return true nếu cập nhật thành công, false nếu không
     */
    @Override
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, role = ?, full_name = ?, email = ?, phone = ?, department = ? WHERE user_id = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getRole());
            statement.setString(4, user.getFullName());
            statement.setString(5, normalizeBlank(user.getEmail()));
            statement.setString(6, normalizeBlank(user.getPhone()));
            statement.setString(7, normalizeBlank(user.getDepartment()));
            statement.setInt(8, user.getUserId());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the cap nhat user.", ex);
        }
    }

    /**
     * Xóa user
     * @param userId ID của user cần xóa
     * @return true nếu xóa thành công, false nếu không
     */
    @Override
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the xoa user.", ex);
        }
    }

    /**
     * Chuyển đổi ResultSet sang đối tượng User
     * @param resultSet ResultSet từ database
     * @return Đối tượng User
     */
    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("user_id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setRole(resultSet.getString("role"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setDepartment(resultSet.getString("department"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        return user;
    }

    /**
     * Chuẩn hóa chuỗi: null nếu rỗng, ngược lại trim
     * @param value Chuỗi cần chuẩn hóa
     * @return Chuỗi đã chuẩn hóa hoặc null
     */
    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
