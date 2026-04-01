package presentation.employee;

import model.BookingDetail;
import model.User;
import service.BookingService;
import util.DateUtil;
import util.InputValidator;

import java.util.List;
import java.util.Scanner;

public class EmployeeBookingViewer {
    private final BookingService bookingService;
    private final Scanner scanner;

    public EmployeeBookingViewer(BookingService bookingService, Scanner scanner) {
        this.bookingService = bookingService;
        this.scanner = scanner;
    }

    public void handleViewMyBookings(User currentUser) {
        List<BookingDetail> details = bookingService.getBookingsByEmployee(currentUser.getUserId());
        if (details.isEmpty()) {
            System.out.println("Ban chua co booking nao.");
            return;
        }

        System.out.println("\n--- BOOKING CUA TOI ---");
        printBookingDetails(details);
    }

    public void handleCancelBooking(User currentUser) {
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

        String bookingIdInput;
        int bookingId = 0;
        while (true) {
            bookingIdInput = InputValidator.promptOptional(scanner, "Nhap bookingId can huy (bo trong de quay lai): ");
            if (bookingIdInput.isBlank()) {
                System.out.println("Da huy thao tac huy booking.");
                return;
            }

            try {
                bookingId = Integer.parseInt(bookingIdInput);
                final int selectedBookingId = bookingId;
                boolean existsInPending = pendingBookings.stream()
                        .anyMatch(detail -> detail.getBookingId() == selectedBookingId);
                if (existsInPending) {
                    break;
                }
                System.out.println("BookingId khong nam trong danh sach PENDING ben tren. Vui long nhap lai.");
            } catch (NumberFormatException ex) {
                System.out.println("Vui long nhap so hop le hoac bo trong de quay lai.");
            }
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
}
