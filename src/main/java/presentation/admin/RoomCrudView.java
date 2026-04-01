package presentation.admin;

import model.Room;
import service.RoomService;
import util.InputValidator;

import java.util.List;
import java.util.Scanner;

public class RoomCrudView {
    private final RoomService roomService;
    private final Scanner scanner;

    public RoomCrudView(RoomService roomService, Scanner scanner) {
        this.roomService = roomService;
        this.scanner = scanner;
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- QUAN LY PHONG ---");
            System.out.println("1. Xem danh sach");
            System.out.println("2. Tim phong theo ten");
            System.out.println("3. Them moi");
            System.out.println("4. Cap nhat");
            System.out.println("5. Xoa (deactivate)");
            System.out.println("0. Quay lai");

            int choice = InputValidator.promptIntInRange(scanner, "Chon: ", 0, 5);
            switch (choice) {
                case 1 -> printRoomsTable(roomService.getAllRooms());
                case 2 -> searchRoomByName();
                case 3 -> createRoom();
                case 4 -> updateRoom();
                case 5 -> deleteRoom();
                case 0 -> running = false;
                default -> {
                }
            }
        }
    }

    private void searchRoomByName() {
        String keyword = InputValidator.promptRequired(scanner, "Nhap ten phong can tim: ");
        List<Room> matchedRooms = roomService.searchRoomsByName(keyword);
        if (matchedRooms.isEmpty()) {
            System.out.println("Khong tim thay phong phu hop.");
        } else {
            System.out.println("\n--- KET QUA TIM KIEM PHONG ---");
            printRoomsTable(matchedRooms);
        }
    }

    private void createRoom() {
        String name = InputValidator.promptRequired(scanner, "Ten phong: ");
        int capacity = InputValidator.promptIntInRange(scanner, "Suc chua: ", 1, Integer.MAX_VALUE);
        String location = InputValidator.promptOptional(scanner, "Vi tri: ");
        String fixedEquipment = InputValidator.promptOptional(scanner, "Thiet bi co dinh: ");
        roomService.createRoom(name, capacity, location, fixedEquipment);
        System.out.println("Them phong thanh cong.");
    }

    private void updateRoom() {
        System.out.println("\nDanh sach phong hien tai:");
        printRoomsTable(roomService.getAllRooms());
        int roomId = InputValidator.promptIntInRange(scanner, "Room ID can cap nhat: ", 1, Integer.MAX_VALUE);
        Room current = roomService.getRoomById(roomId);
        System.out.println("\nThong tin phong hien tai:");
        printRoomsTable(List.of(current));

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

        String fixedEquipment = InputValidator.promptOptional(scanner, "Thiet bi co dinh moi (bo trong de giu): ");
        if (fixedEquipment.isBlank()) {
            fixedEquipment = current.getFixedEquipment();
        }

        int activeChoice = InputValidator.promptIntInRange(scanner, "Trang thai active? (1:active, 0:inactive): ", 0,
                1);
        roomService.updateRoom(roomId, name, capacity, location, fixedEquipment, activeChoice == 1);
        System.out.println("Cap nhat phong thanh cong.");
    }

    private void deleteRoom() {
        System.out.println("\nDanh sach phong hien tai:");
        printRoomsTable(roomService.getAllRooms());
        int roomId = InputValidator.promptIntInRange(scanner, "Room ID can xoa: ", 1, Integer.MAX_VALUE);
        roomService.deactivateRoom(roomId);
        System.out.println("Da deactivate phong.");
    }

    private void printRoomsTable(List<Room> rooms) {
        if (rooms.isEmpty()) {
            System.out.println("Khong co du lieu phong.");
            return;
        }

        String rowFormat = "%-6s %-22s %-10s %-18s %-28s %-8s%n";
        System.out.printf(rowFormat, "ID", "TEN PHONG", "SUC CHUA", "VI TRI", "THIET BI CO DINH", "ACTIVE");
        System.out.println(
                "-----------------------------------------------------------------------------------------------");
        for (Room room : rooms) {
            System.out.printf(rowFormat,
                    room.getRoomId(),
                    truncate(room.getRoomName(), 22),
                    room.getCapacity(),
                    truncate(valueOrDash(room.getLocation()), 18),
                    truncate(valueOrDash(room.getFixedEquipment()), 28),
                    room.isActive() ? "YES" : "NO");
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "-";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
}
