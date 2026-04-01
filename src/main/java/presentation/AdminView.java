package presentation;

import presentation.admin.BookingView;
import presentation.admin.EquipmentCrudView;
import presentation.admin.RoomCrudView;
import presentation.admin.ServiceCrudView;
import presentation.admin.UserCrudView;
import service.AuthService;
import service.BookingService;
import service.EquipmentService;
import service.RoomService;
import util.InputValidator;

import java.util.Scanner;

public class AdminView {
    private final AuthService authService;
    private final RoomService roomService;
    private final EquipmentService equipmentService;
    private final BookingService bookingService;
    private final Scanner scanner;

    private final RoomCrudView roomCrudView;
    private final EquipmentCrudView equipmentCrudView;
    private final ServiceCrudView serviceCrudView;
    private final UserCrudView userCrudView;
    private final BookingView bookingView;

    public AdminView(AuthService authService,
            RoomService roomService,
            EquipmentService equipmentService,
            BookingService bookingService,
            Scanner scanner) {
        this.authService = authService;
        this.roomService = roomService;
        this.equipmentService = equipmentService;
        this.bookingService = bookingService;
        this.scanner = scanner;

        this.roomCrudView = new RoomCrudView(roomService, scanner);
        this.equipmentCrudView = new EquipmentCrudView(equipmentService, scanner);
        this.serviceCrudView = new ServiceCrudView(bookingService, scanner);
        this.userCrudView = new UserCrudView(authService, scanner);
        this.bookingView = new BookingView(authService, bookingService, scanner);
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. Quan ly phong hop (CRUD)");
            System.out.println("2. Quan ly thiet bi (CRUD)");
            System.out.println("3. Quan ly dich vu (CRUD)");
            System.out.println("4. Quan ly user (CRUD)");
            System.out.println("5. Duyet/Tu choi booking PENDING");
            System.out.println("6. Xem tat ca booking");
            System.out.println("7. Tao tai khoan SUPPORT/ADMIN");
            System.out.println("0. Dang xuat");

            int choice = InputValidator.promptIntInRange(scanner, "Chon: ", 0, 7);
            try {
                switch (choice) {
                    case 1 -> roomCrudView.showMenu();
                    case 2 -> equipmentCrudView.showMenu();
                    case 3 -> serviceCrudView.showMenu();
                    case 4 -> userCrudView.showMenu();
                    case 5 -> bookingView.handleApproveOrRejectBooking();
                    case 6 -> bookingView.showAllBookings();
                    case 7 -> createSupportOrAdmin();
                    case 0 -> running = false;
                    default -> {
                    }
                }
            } catch (Exception ex) {
                System.out.println("[LOI] " + ex.getMessage());
            }
        }
    }

    private void createSupportOrAdmin() {
        int roleChoice = InputValidator.promptIntInRange(scanner,
                "Loai tai khoan (1:SUPPORT_STAFF, 2:ADMIN): ", 1, 2);
        String role = switch (roleChoice) {
            case 1 -> "SUPPORT_STAFF";
            case 2 -> "ADMIN";
            default -> throw new IllegalStateException("Gia tri role khong hop le.");
        };

        String username = InputValidator.promptRequired(scanner, "Username: ");
        String password = InputValidator.promptRequired(scanner, "Password (>=6): ");
        String fullName = InputValidator.promptRequired(scanner, "Ho ten: ");
        String email = InputValidator.promptOptional(scanner, "Email: ");
        String phone = InputValidator.promptOptional(scanner, "Phone: ");

        authService.createAccountByAdmin(role, username, password, fullName, email, phone);
        System.out.println("Tao tai khoan " + role + " thanh cong.");
    }
}
