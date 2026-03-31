package presentation;

import model.BookingDetail;
import model.Equipment;
import model.Room;
import model.Service;
import model.User;
import service.AuthService;
import service.BookingService;
import service.EquipmentService;
import service.RoomService;
import util.DateUtil;
import util.InputValidator;

import java.util.List;
import java.util.Scanner;

public class AdminView {
    private final AuthService authService;
    private final RoomService roomService;
    private final EquipmentService equipmentService;
    private final BookingService bookingService;
    private final Scanner scanner;

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
            System.out.println("7. Tao tai khoan EMPLOYEE/SUPPORT/ADMIN");
            System.out.println("0. Dang xuat");

            int choice = InputValidator.promptIntInRange(scanner, "Chon: ", 0, 7);
            try {
                switch (choice) {
                    case 1 -> roomCrudMenu();
                    case 2 -> equipmentCrudMenu();
                    case 3 -> serviceCrudMenu();
                    case 4 -> userCrudMenu();
                    case 5 -> handleApproveOrRejectBooking();
                    case 6 -> printBookings(bookingService.getAllBookings());
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

    private void roomCrudMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- QUAN LY PHONG ---");
            System.out.println("1. Xem danh sach");
            System.out.println("2. Them moi");
            System.out.println("3. Cap nhat");
            System.out.println("4. Xoa (deactivate)");
            System.out.println("0. Quay lai");

            int choice = InputValidator.promptIntInRange(scanner, "Chon: ", 0, 4);
            switch (choice) {
                case 1 -> roomService.getAllRooms().forEach(System.out::println);
                case 2 -> {
                    String name = InputValidator.promptRequired(scanner, "Ten phong: ");
                    int capacity = InputValidator.promptIntInRange(scanner, "Suc chua: ", 1, Integer.MAX_VALUE);
                    String location = InputValidator.promptOptional(scanner, "Vi tri: ");
                    String fixedEquipment = InputValidator.promptOptional(scanner, "Thiet bi co dinh: ");
                    roomService.createRoom(name, capacity, location, fixedEquipment);
                    System.out.println("Them phong thanh cong.");
                }
                case 3 -> {
                    int roomId = InputValidator.promptIntInRange(scanner, "Room ID can cap nhat: ", 1,
                            Integer.MAX_VALUE);
                    Room current = roomService.getRoomById(roomId);

                    String name = InputValidator.promptOptional(scanner, "Ten phong moi (bo trong de giu): ");
                    if (name.isBlank()) {
                        name = current.getRoomName();
                    }
                    Integer capOpt = InputValidator.promptOptionalInt(scanner, "Suc chua moi (bo trong de giu): ");
                    int capacity = capOpt == null ? current.getCapacity() : capOpt;

                    String location = InputValidator.promptOptional(scanner, "Vi tri moi (bo trong de giu): ");
                    if (location.isBlank()) {
                        location = current.getLocation();
                    }

                    String fixedEquipment = InputValidator.promptOptional(scanner,
                            "Thiet bi co dinh moi (bo trong de giu): ");
                    if (fixedEquipment.isBlank()) {
                        fixedEquipment = current.getFixedEquipment();
                    }

                    int activeChoice = InputValidator.promptIntInRange(scanner,
                            "Trang thai active? (1:active, 0:inactive): ", 0, 1);
                    roomService.updateRoom(roomId, name, capacity, location, fixedEquipment, activeChoice == 1);
                    System.out.println("Cap nhat phong thanh cong.");
                }
                case 4 -> {
                    int roomId = InputValidator.promptIntInRange(scanner, "Room ID can xoa: ", 1, Integer.MAX_VALUE);
                    roomService.deactivateRoom(roomId);
                    System.out.println("Da deactivate phong.");
                }
                case 0 -> running = false;
                default -> {
                }
            }
        }
    }

    private void equipmentCrudMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- QUAN LY THIET BI ---");
            System.out.println("1. Xem danh sach");
            System.out.println("2. Them moi");
            System.out.println("3. Cap nhat");
            System.out.println("4. Xoa");
            System.out.println("0. Quay lai");

            int choice = InputValidator.promptIntInRange(scanner, "Chon: ", 0, 4);
            switch (choice) {
                case 1 -> equipmentService.getAllEquipments().forEach(System.out::println);
                case 2 -> {
                    String name = InputValidator.promptRequired(scanner, "Ten thiet bi: ");
                    int total = InputValidator.promptIntInRange(scanner, "Tong so luong: ", 1, Integer.MAX_VALUE);
                    int available = InputValidator.promptIntInRange(scanner, "So luong kha dung: ", 0, total);
                    String status = readEquipmentStatus();
                    equipmentService.createEquipment(name, total, available, status);
                    System.out.println("Them thiet bi thanh cong.");
                }
                case 3 -> {
                    int equipmentId = InputValidator.promptIntInRange(scanner, "Equipment ID can cap nhat: ", 1,
                            Integer.MAX_VALUE);
                    Equipment current = equipmentService.getEquipmentById(equipmentId);

                    String name = InputValidator.promptOptional(scanner, "Ten thiet bi moi (bo trong de giu): ");
                    if (name.isBlank()) {
                        name = current.getEquipmentName();
                    }

                    Integer totalOpt = InputValidator.promptOptionalInt(scanner,
                            "Tong so luong moi (bo trong de giu): ");
                    int total = totalOpt == null ? current.getTotalQuantity() : totalOpt;

                    Integer availableOpt = InputValidator.promptOptionalInt(scanner,
                            "So luong kha dung moi (bo trong de giu): ");
                    int available = availableOpt == null ? current.getAvailableQuantity() : availableOpt;

                    System.out.print("Cap nhat status? (1:Co, 0:Khong): ");
                    int updateStatus = InputValidator.promptIntInRange(scanner, "", 0, 1);
                    String status = current.getStatus();
                    if (updateStatus == 1) {
                        status = readEquipmentStatus();
                    }

                    equipmentService.updateEquipment(equipmentId, name, total, available, status);
                    System.out.println("Cap nhat thiet bi thanh cong.");
                }
                case 4 -> {
                    int equipmentId = InputValidator.promptIntInRange(scanner, "Equipment ID can xoa: ", 1,
                            Integer.MAX_VALUE);
                    equipmentService.deleteEquipment(equipmentId);
                    System.out.println("Xoa thiet bi thanh cong.");
                }
                case 0 -> running = false;
                default -> {
                }
            }
        }
    }

    private void serviceCrudMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- QUAN LY DICH VU ---");
            System.out.println("1. Xem danh sach");
            System.out.println("2. Them moi");
            System.out.println("3. Cap nhat");
            System.out.println("4. Xoa");
            System.out.println("0. Quay lai");

            int choice = InputValidator.promptIntInRange(scanner, "Chon: ", 0, 4);
            switch (choice) {
                case 1 -> bookingService.getAllServices().forEach(System.out::println);
                case 2 -> {
                    String name = InputValidator.promptRequired(scanner, "Ten dich vu: ");
                    double price = promptDouble("Don gia: ");
                    String unit = InputValidator.promptOptional(scanner, "Don vi tinh: ");
                    bookingService.createService(name, price, unit);
                    System.out.println("Them dich vu thanh cong.");
                }
                case 3 -> {
                    int serviceId = InputValidator.promptIntInRange(scanner, "Service ID can cap nhat: ", 1,
                            Integer.MAX_VALUE);
                    List<Service> services = bookingService.getAllServices();
                    Service current = services.stream()
                            .filter(service -> service.getServiceId() == serviceId)
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Khong tim thay dich vu."));

                    String name = InputValidator.promptOptional(scanner, "Ten dich vu moi (bo trong de giu): ");
                    if (name.isBlank()) {
                        name = current.getServiceName();
                    }

                    String priceRaw = InputValidator.promptOptional(scanner, "Don gia moi (bo trong de giu): ");
                    double price = priceRaw.isBlank() ? current.getPrice()
                            : parseDoubleStrict(priceRaw, "Don gia khong hop le.");

                    String unit = InputValidator.promptOptional(scanner, "Don vi tinh moi (bo trong de giu): ");
                    if (unit.isBlank()) {
                        unit = current.getUnit();
                    }

                    bookingService.updateService(serviceId, name, price, unit);
                    System.out.println("Cap nhat dich vu thanh cong.");
                }
                case 4 -> {
                    int serviceId = InputValidator.promptIntInRange(scanner, "Service ID can xoa: ", 1,
                            Integer.MAX_VALUE);
                    bookingService.deleteService(serviceId);
                    System.out.println("Xoa dich vu thanh cong.");
                }
                case 0 -> running = false;
                default -> {
                }
            }
        }
    }

    private void userCrudMenu() {
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
                case 1 -> {
                    System.out.println("\n--- DANH SACH USER ---");
                    authService.getAllUsers().forEach(System.out::println);
                }
                case 2 -> {
                    int roleChoice = InputValidator.promptIntInRange(scanner, 
                            "Loai tai khoan (1:EMPLOYEE, 2:SUPPORT_STAFF, 3:ADMIN): ", 1, 3);
                    String role = switch (roleChoice) {
                        case 1 -> "EMPLOYEE";
                        case 2 -> "SUPPORT_STAFF";
                        case 3 -> "ADMIN";
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
                case 3 -> {
                    int userId = InputValidator.promptIntInRange(scanner, "User ID can cap nhat: ", 1, Integer.MAX_VALUE);
                    User current = authService.getAllUsers().stream()
                            .filter(user -> user.getUserId() == userId)
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user."));

                    System.out.println("User hien tai: " + current);

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
                case 4 -> {
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
                case 0 -> running = false;
                default -> {
                }
            }
        }
    }

    private void handleApproveOrRejectBooking() {
        List<BookingDetail> pending = bookingService.getPendingBookings();
        if (pending.isEmpty()) {
            System.out.println("Khong co booking PENDING nao.");
            return;
        }

        printBookings(pending);
        int bookingId = InputValidator.promptIntInRange(scanner, "Nhap bookingId can xu ly: ", 1, Integer.MAX_VALUE);
        int action = InputValidator.promptIntInRange(scanner, "Chon hanh dong (1:Duyet, 2:Tu choi): ", 1, 2);

        if (action == 1) {
            List<User> supportUsers = authService.getSupportStaffUsers();
            if (supportUsers.isEmpty()) {
                throw new IllegalStateException("Chua co tai khoan SUPPORT_STAFF de phan cong.");
            }
            System.out.println("Danh sach support staff:");
            supportUsers.forEach(user -> System.out.println(user.getUserId() + " - " + user.getFullName()));
            int supportId = InputValidator.promptIntInRange(scanner, "Nhap support staff id: ", 1, Integer.MAX_VALUE);
            bookingService.approveBooking(bookingId, supportId);
            System.out.println("Duyet booking thanh cong.");
        } else {
            bookingService.rejectBooking(bookingId);
            System.out.println("Tu choi booking thanh cong.");
        }
    }

    private void createSupportOrAdmin() {
        int roleChoice = InputValidator.promptIntInRange(scanner, "Loai tai khoan (1:EMPLOYEE, 2:SUPPORT_STAFF, 3:ADMIN): ", 1, 3);
        String role = switch (roleChoice) {
            case 1 -> "EMPLOYEE";
            case 2 -> "SUPPORT_STAFF";
            case 3 -> "ADMIN";
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

    private void printBookings(List<BookingDetail> bookings) {
        if (bookings.isEmpty()) {
            System.out.println("Khong co du lieu booking.");
            return;
        }
        System.out.println("\n--- DANH SACH BOOKING ---");
        for (BookingDetail booking : bookings) {
            System.out.println("----------------------------------------");
            System.out.println("Booking ID : " + booking.getBookingId());
            System.out.println("Nhan vien  : " + booking.getEmployeeName());
            System.out.println("Phong      : " + booking.getRoomName());
            System.out.println("Bat dau    : " + DateUtil.formatDateTime(booking.getStartTime()));
            System.out.println("Ket thuc   : " + DateUtil.formatDateTime(booking.getEndTime()));
            System.out.println("Trang thai : " + booking.getBookingStatus());
            System.out.println("Prep status: " + booking.getPrepStatus());
            System.out.println("Support    : " + valueOrDash(booking.getSupportStaffName()));
            System.out.println("Thiet bi   : " + valueOrDash(booking.getEquipmentSummary()));
            System.out.println("Dich vu    : " + valueOrDash(booking.getServiceSummary()));
        }
    }

    private String readEquipmentStatus() {
        int choice = InputValidator.promptIntInRange(scanner, "Status (1:ACTIVE, 2:MAINTENANCE, 3:INACTIVE): ", 1, 3);
        return switch (choice) {
            case 1 -> "ACTIVE";
            case 2 -> "MAINTENANCE";
            case 3 -> "INACTIVE";
            default -> throw new IllegalStateException("Gia tri status khong hop le.");
        };
    }

    private double promptDouble(String label) {
        while (true) {
            System.out.print(label);
            String raw = scanner.nextLine().trim();
            try {
                return parseDoubleStrict(raw, "Gia tri so khong hop le.");
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private double parseDoubleStrict(String value, String message) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
}
