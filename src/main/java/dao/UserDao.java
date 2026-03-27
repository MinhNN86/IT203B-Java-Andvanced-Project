package dao;

import model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao extends BaseDao {
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tim user theo username.", ex);
        }
        return Optional.empty();
    }

    public Optional<User> findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the tim user theo id.", ex);
        }
        return Optional.empty();
    }

    public boolean createUser(User user) {
        String sql = "INSERT INTO users(username, password, role, full_name, email, phone, department) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

    public boolean updateProfile(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, department = ? WHERE user_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
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

    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY full_name";
        List<User> users = new ArrayList<>();
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setString(1, role);
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach user theo role.", ex);
        }
        return users;
    }

    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY full_name";
        List<User> users = new ArrayList<>();
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the lay danh sach tat ca user.", ex);
        }
        return users;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, role = ?, full_name = ?, email = ?, phone = ?, department = ? WHERE user_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
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

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (var connection = getConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Khong the xoa user.", ex);
        }
    }

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

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
