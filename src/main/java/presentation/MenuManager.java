package presentation;

import model.User;
import service.AuthService;
import service.BookingService;
import service.EquipmentService;
import service.RoomService;
import util.InputValidator;

import java.util.Scanner;

public class MenuManager {
    private final Scanner scanner = new Scanner(System.in);

    private final AuthService authService = new AuthService();
    private final RoomService roomService = new RoomService();
    private final EquipmentService equipmentService = new EquipmentService();
    private final BookingService bookingService = new BookingService();

    private final EmployeeView employeeView = new EmployeeView(
            authService,
            roomService,
            equipmentService,
            bookingService,
            scanner);
    private final SupportView supportView = new SupportView(bookingService, scanner);
    private final AdminView adminView = new AdminView(
            authService,
            roomService,
            equipmentService,
            bookingService,
            scanner);

    public void start() {
        authService.ensureDefaultAdmin();

        boolean running = true;
        while (running) {
            System.out.println("\n===== MEETING MANAGER =====");
            System.out.println("1. Dang ky (Employee)");
            System.out.println("2. Dang nhap");
            System.out.println("0. Thoat");

            int choice = InputValidator.promptIntInRange(scanner, "Chon: ", 0, 2);
            try {
                switch (choice) {
                    case 1 -> handleRegisterEmployee();
                    case 2 -> handleLogin();
                    case 0 -> running = false;
                    default -> {
                    }
                }
            } catch (Exception ex) {
                System.out.println("[LOI] " + ex.getMessage());
            }
        }

        System.out.println("Cam on ban da su dung he thong.");
    }

    private void handleRegisterEmployee() {
        String username = InputValidator.promptRequired(scanner, "Username: ");
        String password = InputValidator.promptRequired(scanner, "Password (>=6): ");
        String fullName = InputValidator.promptRequired(scanner, "Ho ten: ");
        String email = InputValidator.promptOptional(scanner, "Email: ");
        String phone = InputValidator.promptOptional(scanner, "Phone: ");
        String department = InputValidator.promptOptional(scanner, "Phong ban: ");

        authService.registerEmployee(username, password, fullName, email, phone, department);
        System.out.println("Dang ky thanh cong. Ban co the dang nhap ngay bay gio.");
    }

    private void handleLogin() {
        String username = InputValidator.promptRequired(scanner, "Username: ");
        String password = InputValidator.promptRequired(scanner, "Password: ");

        User user = authService.login(username, password);
        System.out.println("Dang nhap thanh cong. Xin chao " + user.getFullName() + " (" + user.getRole() + ")");

        switch (user.getRole()) {
            case "EMPLOYEE" -> employeeView.showMenu(user);
            case "SUPPORT_STAFF" -> supportView.showMenu(user);
            case "ADMIN" -> adminView.showMenu();
            default -> throw new IllegalStateException("Vai tro khong hop le trong he thong: " + user.getRole());
        }
    }
}
