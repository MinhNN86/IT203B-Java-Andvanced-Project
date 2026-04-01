package presentation;

import model.BookingDetail;
import model.User;
import service.BookingService;
import util.DateUtil;
import util.InputValidator;

import java.util.List;
import java.util.Scanner;

public class SupportView {
    private final BookingService bookingService;
    private final Scanner scanner;

    public SupportView(BookingService bookingService, Scanner scanner) {
        this.bookingService = bookingService;
        this.scanner = scanner;
    }

    public void showMenu(User currentUser) {
        boolean running = true;
        while (running) {
            System.out.println("\n===== SUPPORT MENU =====");
            System.out.println("1. Xem booking duoc phan cong");
            System.out.println("2. Cap nhat trang thai chuan bi");
            System.out.println("0. Dang xuat");

            int choice = InputValidator.promptIntInRange(scanner, "Chon: ", 0, 2);
            try {
                switch (choice) {
                    case 1 -> printAssignedBookings(currentUser.getUserId());
                    case 2 -> updatePreparationStatus(currentUser.getUserId());
                    case 0 -> running = false;
                    default -> {
                    }
                }
            } catch (Exception ex) {
                System.out.println("[LOI] " + ex.getMessage());
            }
        }
    }

    private void printAssignedBookings(int supportStaffId) {
        List<BookingDetail> details = bookingService.getBookingsBySupportStaff(supportStaffId);
        if (details.isEmpty()) {
            System.out.println("Ban chua duoc phan cong booking nao.");
            return;
        }

        printBookingDetails(details);
    }

    private void updatePreparationStatus(int supportStaffId) {
        List<BookingDetail> details = bookingService.getBookingsBySupportStaff(supportStaffId);
        if (details.isEmpty()) {
            System.out.println("Ban chua duoc phan cong booking nao.");
            return;
        }

        System.out.println("\n--- BOOKING DUOC PHAN CONG ---");
        printBookingDetails(details);

        int bookingId;
        while (true) {
            bookingId = InputValidator.promptIntInRange(scanner, "Nhap bookingId can cap nhat: ", 1, Integer.MAX_VALUE);
            final int selectedBookingId = bookingId;
            boolean assignedToCurrentSupport = details.stream()
                    .anyMatch(detail -> detail.getBookingId() == selectedBookingId);
            if (assignedToCurrentSupport) {
                break;
            }
            System.out.println("BookingId khong nam trong danh sach duoc phan cong. Vui long nhap lai.");
        }

        int statusChoice = InputValidator.promptIntInRange(
                scanner,
                "Trang thai (1:PREPARING, 2:READY, 3:MISSING_EQUIPMENT): ",
                1,
                3);

        String prepStatus = switch (statusChoice) {
            case 1 -> "PREPARING";
            case 2 -> "READY";
            case 3 -> "MISSING_EQUIPMENT";
            default -> throw new IllegalStateException("Trang thai khong hop le.");
        };

        bookingService.updatePreparationStatus(bookingId, supportStaffId, prepStatus);
        System.out.println("Cap nhat prep status thanh cong.");
    }

    private void printBookingDetails(List<BookingDetail> details) {
        for (BookingDetail detail : details) {
            System.out.println("----------------------------------------");
            System.out.println("Booking ID : " + detail.getBookingId());
            System.out.println("Nhan vien  : " + detail.getEmployeeName());
            System.out.println("Phong      : " + detail.getRoomName());
            System.out.println("Bat dau    : " + DateUtil.formatDateTime(detail.getStartTime()));
            System.out.println("Ket thuc   : " + DateUtil.formatDateTime(detail.getEndTime()));
            System.out.println("Trang thai : " + detail.getBookingStatus());
            System.out.println("Prep status: " + detail.getPrepStatus());
            System.out.println("Thiet bi   : " + valueOrDash(detail.getEquipmentSummary()));
            System.out.println("Dich vu    : " + valueOrDash(detail.getServiceSummary()));
        }
    }

    private String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
}
