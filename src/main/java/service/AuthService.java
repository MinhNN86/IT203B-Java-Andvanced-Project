package service;

import dao.UserDao;
import model.User;
import util.InputValidator;
import util.SecurityUtil;

import java.util.List;
import java.util.Optional;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public User registerEmployee(String username,
            String plainPassword,
            String fullName,
            String email,
            String phone,
            String department) {
        validateCredentials(username, plainPassword);
        validateContact(email, phone);

        if (userDao.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username da ton tai.");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(SecurityUtil.hashPassword(plainPassword));
        user.setRole("EMPLOYEE");
        user.setFullName(fullName.trim());
        user.setEmail(email);
        user.setPhone(phone);
        user.setDepartment(department);

        userDao.createUser(user);
        return user;
    }

    public User createAccountByAdmin(String role,
            String username,
            String plainPassword,
            String fullName,
            String email,
            String phone,
            String department) {
        if (!"SUPPORT_STAFF".equals(role) && !"ADMIN".equals(role)) {
            throw new IllegalArgumentException("Admin chi duoc tao SUPPORT_STAFF hoac ADMIN.");
        }

        validateCredentials(username, plainPassword);
        validateContact(email, phone);

        if (userDao.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username da ton tai.");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(SecurityUtil.hashPassword(plainPassword));
        user.setRole(role);
        user.setFullName(fullName.trim());
        user.setEmail(email);
        user.setPhone(phone);
        user.setDepartment(department);

        userDao.createUser(user);
        return user;
    }

    public User login(String username, String plainPassword) {
        Optional<User> userOpt = userDao.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Sai username hoac mat khau.");
        }

        User user = userOpt.get();
        if (!SecurityUtil.verifyPassword(plainPassword, user.getPassword())) {
            throw new IllegalArgumentException("Sai username hoac mat khau.");
        }
        return user;
    }

    public User updateProfile(User currentUser,
            String fullName,
            String email,
            String phone,
            String department) {
        validateContact(email, phone);

        currentUser.setFullName(fullName.trim());
        currentUser.setEmail(email);
        currentUser.setPhone(phone);
        currentUser.setDepartment(department);

        boolean updated = userDao.updateProfile(currentUser);
        if (!updated) {
            throw new IllegalStateException("Cap nhat profile that bai.");
        }

        return userDao.findById(currentUser.getUserId())
                .orElseThrow(() -> new IllegalStateException("Khong tai lai duoc profile."));
    }

    public List<User> getSupportStaffUsers() {
        return userDao.findByRole("SUPPORT_STAFF");
    }

    public void ensureDefaultAdmin() {
        List<User> admins = userDao.findByRole("ADMIN");
        if (!admins.isEmpty()) {
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(SecurityUtil.hashPassword("admin123"));
        admin.setRole("ADMIN");
        admin.setFullName("System Admin");
        admin.setEmail("admin@local");
        admin.setPhone("0000000000");
        admin.setDepartment("IT");
        userDao.createUser(admin);

        System.out.println("[INFO] Da tao tai khoan ADMIN mac dinh: admin / admin123");
    }

    public User updateUserByAdmin(int userId,
            String username,
            String plainPassword,
            String role,
            String fullName,
            String email,
            String phone,
            String department) {
        validateRole(role);
        validateCredentials(username, plainPassword);
        validateContact(email, phone);

        User existingUser = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user."));

        // Check if username is being changed and if it conflicts with another user
        if (!existingUser.getUsername().equals(username)) {
            if (userDao.findByUsername(username).isPresent()) {
                throw new IllegalArgumentException("Username da ton tai.");
            }
        }

        existingUser.setUsername(username.trim());
        existingUser.setPassword(SecurityUtil.hashPassword(plainPassword));
        existingUser.setRole(role);
        existingUser.setFullName(fullName.trim());
        existingUser.setEmail(email);
        existingUser.setPhone(phone);
        existingUser.setDepartment(department);

        boolean updated = userDao.updateUser(existingUser);
        if (!updated) {
            throw new IllegalStateException("Cap nhat user that bai.");
        }

        return userDao.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Khong tai lai duoc user."));
    }

    public boolean deleteUserByAdmin(int userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user."));

        // Prevent deleting the last admin
        if ("ADMIN".equals(user.getRole())) {
            List<User> admins = userDao.findByRole("ADMIN");
            if (admins.size() <= 1) {
                throw new IllegalStateException("Khong the xoa admin cuoi cung.");
            }
        }

        return userDao.deleteUser(userId);
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    private void validateCredentials(String username, String plainPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username khong duoc de trong.");
        }
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("Mat khau toi thieu 6 ky tu.");
        }
    }

    private void validateRole(String role) {
        if (!"EMPLOYEE".equals(role) && !"SUPPORT_STAFF".equals(role) && !"ADMIN".equals(role)) {
            throw new IllegalArgumentException("Role khong hop le. Phai la EMPLOYEE, SUPPORT_STAFF, hoac ADMIN.");
        }
    }

    private void validateContact(String email, String phone) {
        if (!InputValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Email khong hop le.");
        }
        if (!InputValidator.isValidPhone(phone)) {
            throw new IllegalArgumentException("So dien thoai khong hop le.");
        }
    }
}
