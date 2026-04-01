package presentation.admin;

import model.BookingDetail;
import model.User;
import service.AuthService;
import service.BookingService;
import util.DateUtil;
import util.InputValidator;

import java.util.List;
import java.util.Scanner;

public class BookingView {
    private final AuthService authService;
    private final BookingService bookingService;
    private final Scanner scanner;

    public BookingView(AuthService authService, BookingService bookingService, Scanner scanner) {
        this.authService = authService;
        this.bookingService = bookingService;
        this.scanner = scanner;
    }

    public void showAllBookings() {
        printBookings(bookingService.getAllBookings());
    }

    public void handleApproveOrRejectBooking() {
        List<BookingDetail> pending = bookingService.getPendingBookings();
        if (pending.isEmpty()) {
            System.out.println("Khong co booking PENDING nao.");
            return;
        }

        printBookings(pending);
        Integer bookingId = InputValidator.promptOptionalInt(scanner, "Nhap bookingId can xu ly (Enter de bo qua): ");
        if (bookingId == null) {
            System.out.println("Da bo qua. Tro ve menu chinh.");
            return;
        }

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

    private String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
}
