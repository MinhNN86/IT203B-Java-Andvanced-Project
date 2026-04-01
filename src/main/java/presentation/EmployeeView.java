package presentation;

import model.User;
import presentation.employee.EmployeeBookingHandler;
import presentation.employee.EmployeeBookingViewer;
import presentation.employee.EmployeeInputPrompter;
import presentation.employee.EmployeeProfileHandler;
import service.AuthService;
import service.BookingService;
import service.EquipmentService;
import service.RoomService;
import util.InputValidator;

import java.util.Scanner;

public class EmployeeView {
    private final AuthService authService;
    private final RoomService roomService;
    private final EquipmentService equipmentService;
    private final BookingService bookingService;
    private final Scanner scanner;

    private final EmployeeProfileHandler profileHandler;
    private final EmployeeBookingHandler bookingHandler;
    private final EmployeeBookingViewer bookingViewer;
    private final EmployeeInputPrompter inputPrompter;

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

        this.inputPrompter = new EmployeeInputPrompter(scanner);
        this.profileHandler = new EmployeeProfileHandler(authService, scanner);
        this.bookingViewer = new EmployeeBookingViewer(bookingService, scanner);
        this.bookingHandler = new EmployeeBookingHandler(roomService, equipmentService, bookingService, inputPrompter,
                scanner);
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
                    case 1 -> currentUser = profileHandler.handleProfile(currentUser);
                    case 2 -> bookingHandler.handleCreateBooking(currentUser);
                    case 3 -> bookingViewer.handleViewMyBookings(currentUser);
                    case 4 -> bookingViewer.handleCancelBooking(currentUser);
                    case 0 -> running = false;
                    default -> {
                    }
                }
            } catch (Exception ex) {
                System.out.println("[LOI] " + ex.getMessage());
            }
        }
    }
}
