package presentation.employee;

import model.Equipment;
import model.Room;
import model.Service;
import util.DateUtil;
import util.InputValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EmployeeInputPrompter {
    private final Scanner scanner;

    public EmployeeInputPrompter(Scanner scanner) {
        this.scanner = scanner;
    }

    public Map<Integer, Integer> promptEquipmentRequests(List<Equipment> equipments) {
        Map<Integer, Integer> requests = new java.util.LinkedHashMap<>();
        if (equipments.isEmpty()) {
            return requests;
        }

        while (true) {
            String choice = InputValidator.promptOptional(scanner,
                    "Chon thiet bi can muon (nhap id, hoac 'x' de ket thuc): ");
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

    public Map<Integer, Integer> promptServiceRequests(List<Service> services) {
        Map<Integer, Integer> requests = new java.util.LinkedHashMap<>();
        if (services.isEmpty()) {
            return requests;
        }

        while (true) {
            String choice = InputValidator.promptOptional(scanner,
                    "Chon dich vu can dung (nhap id, hoac 'x' de ket thuc): ");
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

    public Room promptActiveRoom(List<Room> rooms) {
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

    public int promptAttendeesForRoom(Room room) {
        while (true) {
            int attendees = InputValidator.promptIntInRange(scanner, "So nguoi du kien: ", 1, Integer.MAX_VALUE);
            if (attendees <= room.getCapacity()) {
                return attendees;
            }
            System.out.println("So nguoi vuot suc chua phong (toi da " + room.getCapacity() + "). Vui long nhap lai.");
        }
    }

    public LocalDateTime promptFutureStartTime() {
        while (true) {
            LocalDateTime startTime = InputValidator.promptDateTime(scanner, "Thoi gian bat dau");
            if (DateUtil.isFuture(startTime)) {
                return startTime;
            }
            System.out.println("Thoi gian bat dau phai o tuong lai. Vui long nhap lai.");
        }
    }

    public LocalDateTime promptEndTimeAfterStart(LocalDateTime startTime) {
        while (true) {
            LocalDateTime endTime = InputValidator.promptDateTime(scanner, "Thoi gian ket thuc");
            if (DateUtil.isValidRange(startTime, endTime)) {
                return endTime;
            }
            System.out.println("Thoi gian ket thuc phai sau thoi gian bat dau. Vui long nhap lai.");
        }
    }
}
