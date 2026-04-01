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

        String fullNameInput = InputValidator.promptOptional(scanner, "Ho ten moi (bo trong de giu): ");
        String emailInput = InputValidator.promptOptional(scanner, "Email moi (bo trong de giu): ");
        String phoneInput = InputValidator.promptOptional(scanner, "Phone moi (bo trong de giu): ");

        String fullName = fullNameInput.isBlank() ? currentUser.getFullName() : fullNameInput;
        String email = emailInput.isBlank() ? currentUser.getEmail() : emailInput;
        String phone = phoneInput.isBlank() ? currentUser.getPhone() : phoneInput;

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
        rooms.forEach(System.out::println);

        Room selectedRoom = promptActiveRoom(rooms);
        int attendees = promptAttendeesForRoom(selectedRoom);
        LocalDateTime startTime = promptFutureStartTime();
        LocalDateTime endTime = promptEndTimeAfterStart(startTime);

        List<Equipment> equipments = equipmentService.getAllEquipments();
        System.out.println("\n--- DANH SACH THIET BI ---");
        if (equipments.isEmpty()) {
            System.out.println("(Khong co thiet bi)");
        } else {
            equipments.forEach(System.out::println);
        }
        Map<Integer, Integer> equipmentRequests = promptEquipmentRequests(equipments);

        List<Service> services = bookingService.getAllServices();
        System.out.println("\n--- DANH SACH DICH VU ---");
        if (services.isEmpty()) {
            System.out.println("(Khong co dich vu)");
        } else {
            services.forEach(System.out::println);
        }
        Map<Integer, Integer> serviceRequests = promptServiceRequests(services);

        while (true) {
            try {
                int bookingId = bookingService.createBooking(
                        currentUser.getUserId(),
                        selectedRoom.getRoomId(),
                        startTime,
                        endTime,
                        attendees,
                        equipmentRequests,
                        serviceRequests);

                System.out.println("Tao yeu cau booking thanh cong. Booking ID = " + bookingId + " (trang thai PENDING)");
                return;
            } catch (IllegalArgumentException ex) {
                System.out.println("[LOI] " + ex.getMessage());
                int retry = InputValidator.promptIntInRange(scanner,
                        "Nhap lai thong tin dat phong? (1:Co, 0:Huy): ", 0, 1);
                if (retry == 0) {
                    System.out.println("Da huy thao tac dat phong.");
                    return;
                }

                // Re-enter only fields that commonly cause booking business-rule conflicts.
                attendees = promptAttendeesForRoom(selectedRoom);
                startTime = promptFutureStartTime();
                endTime = promptEndTimeAfterStart(startTime);
            }
        }
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
        List<BookingDetail> myBookings = bookingService.getBookingsByEmployee(currentUser.getUserId());
        if (myBookings.isEmpty()) {
            System.out.println("Ban chua co booking nao.");
            return;
        }

        List<BookingDetail> pendingBookings = myBookings.stream()
                .filter(detail -> "PENDING".equals(detail.getBookingStatus()))
                .toList();

        if (pendingBookings.isEmpty()) {
            System.out.println("Ban khong co booking PENDING de huy.");
            return;
        }

        System.out.println("\n--- BOOKING PENDING CO THE HUY ---");
        printBookingDetails(pendingBookings);

        int bookingId;
        while (true) {
            bookingId = InputValidator.promptIntInRange(scanner, "Nhap bookingId can huy: ", 1, Integer.MAX_VALUE);
            final int selectedBookingId = bookingId;
            boolean existsInPending = pendingBookings.stream()
                    .anyMatch(detail -> detail.getBookingId() == selectedBookingId);
            if (existsInPending) {
                break;
            }
            System.out.println("BookingId khong nam trong danh sach PENDING ben tren. Vui long nhap lai.");
        }

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

    private Map<Integer, Integer> promptEquipmentRequests(List<Equipment> equipments) {
        Map<Integer, Integer> requests = new java.util.LinkedHashMap<>();
        if (equipments.isEmpty()) {
            return requests;
        }

        while (true) {
            String choice = InputValidator.promptOptional(scanner, "Chon thiet bi can muon (nhap id, hoac 'x' de ket thuc): ");
            if (choice.equalsIgnoreCase("x")) {
                break;
            }
            try {
                int equipmentId = Integer.parseInt(choice);
                Equipment equipment = equipments.stream()
                        .filter(e -> e.getEquipmentId() == equipmentId)
                        .findFirst()
                        .orElse(null);
                
                if (equipment == null) {
                    System.out.println("Thiet bi khong ton tai.");
                    continue;
                }

                int quantity = InputValidator.promptIntInRange(scanner, 
                        "So luong can muon (toi da " + equipment.getAvailableQuantity() + "): ", 
                        1, equipment.getAvailableQuantity());
                requests.put(equipmentId, requests.getOrDefault(equipmentId, 0) + quantity);
                System.out.println("Da them " + quantity + " " + equipment.getEquipmentName());
            } catch (NumberFormatException ex) {
                System.out.println("Vui long nhap so hop le hoac 'x' de ket thuc.");
            }
        }
        return requests;
    }

    private Map<Integer, Integer> promptServiceRequests(List<Service> services) {
        Map<Integer, Integer> requests = new java.util.LinkedHashMap<>();
        if (services.isEmpty()) {
            return requests;
        }

        while (true) {
            String choice = InputValidator.promptOptional(scanner, "Chon dich vu can dung (nhap id, hoac 'x' de ket thuc): ");
            if (choice.equalsIgnoreCase("x")) {
                break;
            }
            try {
                int serviceId = Integer.parseInt(choice);
                Service service = services.stream()
                        .filter(s -> s.getServiceId() == serviceId)
                        .findFirst()
                        .orElse(null);
                
                if (service == null) {
                    System.out.println("Dich vu khong ton tai.");
                    continue;
                }

                int quantity = InputValidator.promptIntInRange(scanner, 
                        "So luong: ", 
                        1, Integer.MAX_VALUE);
                requests.put(serviceId, requests.getOrDefault(serviceId, 0) + quantity);
                System.out.println("Da them " + quantity + " " + service.getServiceName());
            } catch (NumberFormatException ex) {
                System.out.println("Vui long nhap so hop le hoac 'x' de ket thuc.");
            }
        }
        return requests;
    }

    private Room promptActiveRoom(List<Room> rooms) {
        while (true) {
            int roomId = InputValidator.promptIntInRange(scanner, "Nhap roomId: ", 1, Integer.MAX_VALUE);
            Room selectedRoom = rooms.stream()
                    .filter(room -> room.getRoomId() == roomId)
                    .findFirst()
                    .orElse(null);
            if (selectedRoom != null) {
                return selectedRoom;
            }
            System.out.println("RoomId khong hop le hoac phong khong hoat dong. Vui long nhap lai.");
        }
    }

    private int promptAttendeesForRoom(Room room) {
        while (true) {
            int attendees = InputValidator.promptIntInRange(scanner, "So nguoi du kien: ", 1, Integer.MAX_VALUE);
            if (attendees <= room.getCapacity()) {
                return attendees;
            }
            System.out.println("So nguoi vuot suc chua phong (toi da " + room.getCapacity() + "). Vui long nhap lai.");
        }
    }

    private LocalDateTime promptFutureStartTime() {
        while (true) {
            LocalDateTime startTime = InputValidator.promptDateTime(scanner, "Thoi gian bat dau");
            if (DateUtil.isFuture(startTime)) {
                return startTime;
            }
            System.out.println("Thoi gian bat dau phai o tuong lai. Vui long nhap lai.");
        }
    }

    private LocalDateTime promptEndTimeAfterStart(LocalDateTime startTime) {
        while (true) {
            LocalDateTime endTime = InputValidator.promptDateTime(scanner, "Thoi gian ket thuc");
            if (DateUtil.isValidRange(startTime, endTime)) {
                return endTime;
            }
            System.out.println("Thoi gian ket thuc phai sau thoi gian bat dau. Vui long nhap lai.");
        }
    }
}
