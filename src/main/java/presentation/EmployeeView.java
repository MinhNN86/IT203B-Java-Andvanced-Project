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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EmployeeView {
    private final AuthService authService;
    private final RoomService roomService;
    private final EquipmentService equipmentService;
    private final BookingService bookingService;
    private final Scanner scanner;

    public EmployeeView(AuthService authService,
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

    public void showMenu(User currentUser) {
        boolean running = true;
        while (running) {
            System.out.println("\n===== EMPLOYEE MENU =====");
            System.out.println("1. Xem/Cap nhat ho so");
            System.out.println("2. Dat phong hop");
            System.out.println("3. Xem booking cua toi");
            System.out.println("4. Huy booking PENDING");
            System.out.println("0. Dang xuat");

            int choice = InputValidator.promptIntInRange(scanner, "Chon: ", 0, 4);
            try {
                switch (choice) {
                    case 1 -> currentUser = handleProfile(currentUser);
                    case 2 -> handleCreateBooking(currentUser);
                    case 3 -> handleViewMyBookings(currentUser);
                    case 4 -> handleCancelBooking(currentUser);
                    case 0 -> running = false;
                    default -> {
                    }
                }
            } catch (Exception ex) {
                System.out.println("[LOI] " + ex.getMessage());
            }
        }
    }

    private User handleProfile(User currentUser) {
        System.out.println("\n--- HO SO HIEN TAI ---");
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("Ho ten  : " + currentUser.getFullName());
        System.out.println("Email   : " + valueOrDash(currentUser.getEmail()));
        System.out.println("Phone   : " + valueOrDash(currentUser.getPhone()));

        int choice = InputValidator.promptIntInRange(scanner, "Cap nhat ho so? (1:Co, 0:Khong): ", 0, 1);
        if (choice == 0) {
            return currentUser;
        }

        String fullName = InputValidator.promptRequired(scanner, "Ho ten moi: ");
        String email = InputValidator.promptOptional(scanner, "Email moi (co the bo trong): ");
        String phone = InputValidator.promptOptional(scanner, "Phone moi (co the bo trong): ");

        User updated = authService.updateProfile(currentUser, fullName, email, phone);
        System.out.println("Cap nhat ho so thanh cong.");
        return updated;
    }

    private void handleCreateBooking(User currentUser) {
        List<Room> rooms = roomService.getAllRooms().stream().filter(Room::isActive).toList();
        if (rooms.isEmpty()) {
            System.out.println("Khong co phong dang hoat dong.");
            return;
        }

        System.out.println("\n--- DANH SACH PHONG ---");
        rooms.forEach(room -> System.out.println(room));

        int roomId = InputValidator.promptIntInRange(scanner, "Nhap roomId: ", 1, Integer.MAX_VALUE);
        int attendees = InputValidator.promptIntInRange(scanner, "So nguoi du kien: ", 1, Integer.MAX_VALUE);
        LocalDateTime startTime = InputValidator.promptDateTime(scanner, "Thoi gian bat dau");
        LocalDateTime endTime = InputValidator.promptDateTime(scanner, "Thoi gian ket thuc");

        List<Equipment> equipments = equipmentService.getAllEquipments();
        System.out.println("\n--- DANH SACH THIET BI ---");
        if (equipments.isEmpty()) {
            System.out.println("(Khong co thiet bi)");
        } else {
            equipments.forEach(equipment -> System.out.println(equipment));
        }
        String equipmentInput = InputValidator.promptOptional(
                scanner,
                "Nhap thiet bi can muon (id:soLuong,id:soLuong - bo trong neu khong): ");
        Map<Integer, Integer> equipmentRequests = InputValidator.parseItemQuantityMap(equipmentInput);

        List<Service> services = bookingService.getAllServices();
        System.out.println("\n--- DANH SACH DICH VU ---");
        if (services.isEmpty()) {
            System.out.println("(Khong co dich vu)");
        } else {
            services.forEach(service -> System.out.println(service));
        }
        String serviceInput = InputValidator.promptOptional(
                scanner,
                "Nhap dich vu can dung (id:soLuong,id:soLuong - bo trong neu khong): ");
        Map<Integer, Integer> serviceRequests = InputValidator.parseItemQuantityMap(serviceInput);

        int bookingId = bookingService.createBooking(
                currentUser.getUserId(),
                roomId,
                startTime,
                endTime,
                attendees,
                equipmentRequests,
                serviceRequests);

        System.out.println("Tao yeu cau booking thanh cong. Booking ID = " + bookingId + " (trang thai PENDING)");
    }

    private void handleViewMyBookings(User currentUser) {
        List<BookingDetail> details = bookingService.getBookingsByEmployee(currentUser.getUserId());
        if (details.isEmpty()) {
            System.out.println("Ban chua co booking nao.");
            return;
        }

        System.out.println("\n--- BOOKING CUA TOI ---");
        printBookingDetails(details);
    }

    private void handleCancelBooking(User currentUser) {
        int bookingId = InputValidator.promptIntInRange(scanner, "Nhap bookingId can huy: ", 1, Integer.MAX_VALUE);
        bookingService.cancelPendingBooking(bookingId, currentUser.getUserId());
        System.out.println("Huy booking thanh cong.");
    }

    private void printBookingDetails(List<BookingDetail> details) {
        for (BookingDetail detail : details) {
            System.out.println("----------------------------------------");
            System.out.println("Booking ID : " + detail.getBookingId());
            System.out.println("Phong      : " + detail.getRoomName());
            System.out.println("Bat dau    : " + DateUtil.formatDateTime(detail.getStartTime()));
            System.out.println("Ket thuc   : " + DateUtil.formatDateTime(detail.getEndTime()));
            System.out.println("Trang thai : " + detail.getBookingStatus());
            System.out.println("Chuan bi   : " + detail.getPrepStatus());
            System.out.println("Support    : " + valueOrDash(detail.getSupportStaffName()));
            System.out.println("Thiet bi   : " + valueOrDash(detail.getEquipmentSummary()));
            System.out.println("Dich vu    : " + valueOrDash(detail.getServiceSummary()));
        }
    }

    private String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
}
