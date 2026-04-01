package presentation.admin;

import model.User;
import service.AuthService;
import util.InputValidator;

import java.util.List;
import java.util.Scanner;

public class UserCrudView {
    private final AuthService authService;
    private final Scanner scanner;

    public UserCrudView(AuthService authService, Scanner scanner) {
        this.authService = authService;
        this.scanner = scanner;
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- QUAN LY USER ---");
            System.out.println("1. Xem danh sach");
            System.out.println("2. Them moi");
            System.out.println("3. Cap nhat");
            System.out.println("4. Xoa");
            System.out.println("0. Quay lai");

            int choice = InputValidator.promptIntInRange(scanner, "Chon: ", 0, 4);
            switch (choice) {
                case 1 -> printUsersTable(authService.getAllUsers());
                case 2 -> createUser();
                case 3 -> updateUser();
                case 4 -> deleteUser();
                case 0 -> running = false;
                default -> {
                }
            }
        }
    }

    private void createUser() {
        int roleChoice = InputValidator.promptIntInRange(scanner,
                "Loai tai khoan (1:SUPPORT_STAFF, 2:ADMIN): ", 1, 2);
        String role = switch (roleChoice) {
            case 1 -> "SUPPORT_STAFF";
            case 2 -> "ADMIN";
            default -> throw new IllegalStateException("Gia tri role khong hop le.");
        };

        String username = InputValidator.promptRequired(scanner, "Username: ");
        String password = InputValidator.promptRequired(scanner, "Password (>=6 ky tu): ");
        String fullName = InputValidator.promptRequired(scanner, "Ho ten: ");
        String email = InputValidator.promptOptional(scanner, "Email: ");
        String phone = InputValidator.promptOptional(scanner, "Phone: ");

        authService.createAccountByAdmin(role, username, password, fullName, email, phone);
        System.out.println("Tao user thanh cong.");
    }

    private void updateUser() {
        System.out.println("\nDanh sach user hien tai:");
        printUsersTable(authService.getAllUsers());
        int userId = InputValidator.promptIntInRange(scanner, "User ID can cap nhat: ", 1, Integer.MAX_VALUE);
        User current = authService.getAllUsers().stream()
                .filter(user -> user.getUserId() == userId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user."));
        System.out.println("\nThong tin user hien tai:");
        printUsersTable(List.of(current));

        String username = InputValidator.promptOptional(scanner, "Username moi (bo trong de giu): ");
        if (username.isBlank()) {
            username = current.getUsername();
        }

        String password = InputValidator.promptOptional(scanner, "Password moi (bo trong de giu): ");
        if (password.isBlank()) {
            password = current.getPassword();
        }

        Integer roleChoice = InputValidator.promptOptionalInt(scanner,
                "Role moi (1:EMPLOYEE, 2:SUPPORT_STAFF, 3:ADMIN, bo trong de giu): ");
        String role;
        if (roleChoice == null) {
            role = current.getRole();
        } else {
            role = switch (roleChoice) {
                case 1 -> "EMPLOYEE";
                case 2 -> "SUPPORT_STAFF";
                case 3 -> "ADMIN";
                default -> throw new IllegalStateException("Gia tri role khong hop le.");
            };
        }

        String fullName = InputValidator.promptOptional(scanner, "Ho ten moi (bo trong de giu): ");
        if (fullName.isBlank()) {
            fullName = current.getFullName();
        }

        String email = InputValidator.promptOptional(scanner, "Email moi (bo trong de giu): ");
        if (email.isBlank()) {
            email = current.getEmail();
        }

        String phone = InputValidator.promptOptional(scanner, "Phone moi (bo trong de giu): ");
        if (phone.isBlank()) {
            phone = current.getPhone();
        }

        authService.updateUserByAdmin(userId, username, password, role, fullName, email, phone);
        System.out.println("Cap nhat user thanh cong.");
    }

    private void deleteUser() {
        System.out.println("\nDanh sach user hien tai:");
        printUsersTable(authService.getAllUsers());
        int userId = InputValidator.promptIntInRange(scanner, "User ID can xoa: ", 1, Integer.MAX_VALUE);
        User user = authService.getAllUsers().stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user."));

        System.out.println("User se xoa: " + user);
        String confirm = InputValidator.promptOptional(scanner, "Xac nhan xoa? (y/N): ");
        if (confirm.equalsIgnoreCase("y")) {
            authService.deleteUserByAdmin(userId);
            System.out.println("Xoa user thanh cong.");
        } else {
            System.out.println("Da huy xoa user.");
        }
    }

    private void printUsersTable(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("Khong co du lieu user.");
            return;
        }

        String rowFormat = "%-6s %-20s %-15s %-25s %-20s %-15s%n";
        System.out.printf(rowFormat, "ID", "USERNAME", "ROLE", "HO TEN", "EMAIL", "PHONE");
        System.out
                .println("------------------------------------------------------------------------------------------");
        for (User user : users) {
            System.out.printf(rowFormat,
                    user.getUserId(),
                    truncate(user.getUsername(), 20),
                    truncate(user.getRole(), 15),
                    truncate(user.getFullName(), 25),
                    truncate(valueOrDash(user.getEmail()), 20),
                    truncate(valueOrDash(user.getPhone()), 15));
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "-";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
}
