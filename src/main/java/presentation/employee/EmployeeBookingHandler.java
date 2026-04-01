package presentation.employee;

import model.Equipment;
import model.Room;
import model.Service;
import model.User;
import service.BookingService;
import service.EquipmentService;
import service.RoomService;
import util.InputValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EmployeeBookingHandler {
    private final RoomService roomService;
    private final EquipmentService equipmentService;
    private final BookingService bookingService;
    private final EmployeeInputPrompter inputPrompter;
    private final Scanner scanner;

    public EmployeeBookingHandler(RoomService roomService,
            EquipmentService equipmentService,
            BookingService bookingService,
            EmployeeInputPrompter inputPrompter,
            Scanner scanner) {
        this.roomService = roomService;
        this.equipmentService = equipmentService;
        this.bookingService = bookingService;
        this.inputPrompter = inputPrompter;
        this.scanner = scanner;
    }

    public void handleCreateBooking(User currentUser) {
        LocalDateTime startTime = inputPrompter.promptFutureStartTime();
        LocalDateTime endTime = inputPrompter.promptEndTimeAfterStart(startTime);

        List<Integer> availableRoomIds = bookingService.findAvailableRoomIds(startTime, endTime);
        if (availableRoomIds.isEmpty()) {
            System.out.println("Khong co phong trong trong khung gio nay.");
            return;
        }

        List<Room> availableRooms = roomService.getAllRooms().stream()
                .filter(Room::isActive)
                .filter(room -> availableRoomIds.contains(room.getRoomId()))
                .toList();

        System.out.println("\n--- DANH SACH PHONG TRONG ---");
        availableRooms.forEach(System.out::println);

        Room selectedRoom = inputPrompter.promptActiveRoom(availableRooms);
        int attendees = inputPrompter.promptAttendeesForRoom(selectedRoom);

        List<Equipment> equipments = equipmentService.getAllEquipments();
        System.out.println("\n--- DANH SACH THIET BI ---");
        if (equipments.isEmpty()) {
            System.out.println("(Khong co thiet bi)");
        } else {
            equipments.forEach(System.out::println);
        }
        Map<Integer, Integer> equipmentRequests = inputPrompter.promptEquipmentRequests(equipments);

        List<Service> services = bookingService.getAllServices();
        System.out.println("\n--- DANH SACH DICH VU ---");
        if (services.isEmpty()) {
            System.out.println("(Khong co dich vu)");
        } else {
            services.forEach(System.out::println);
        }
        Map<Integer, Integer> serviceRequests = inputPrompter.promptServiceRequests(services);

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

                System.out
                        .println("Tao yeu cau booking thanh cong. Booking ID = " + bookingId + " (trang thai PENDING)");
                return;
            } catch (IllegalArgumentException ex) {
                System.out.println("[LOI] " + ex.getMessage());
                int retry = InputValidator.promptIntInRange(scanner,
                        "Nhap lai thong tin dat phong? (1:Co, 0:Huy): ", 0, 1);
                if (retry == 0) {
                    System.out.println("Da huy thao tac dat phong.");
                    return;
                }

                // Re-enter date/time and get available rooms again
                startTime = inputPrompter.promptFutureStartTime();
                endTime = inputPrompter.promptEndTimeAfterStart(startTime);

                List<Integer> retryRoomIds = bookingService.findAvailableRoomIds(startTime, endTime);
                if (retryRoomIds.isEmpty()) {
                    System.out.println("Khong co phong trong trong khung gio nay.");
                    return;
                }

                List<Room> retryRooms = roomService.getAllRooms().stream()
                        .filter(Room::isActive)
                        .filter(room -> retryRoomIds.contains(room.getRoomId()))
                        .toList();

                System.out.println("\n--- DANH SACH PHONG TRONG ---");
                retryRooms.forEach(System.out::println);

                selectedRoom = inputPrompter.promptActiveRoom(retryRooms);
                attendees = inputPrompter.promptAttendeesForRoom(selectedRoom);
            }
        }
    }
}
