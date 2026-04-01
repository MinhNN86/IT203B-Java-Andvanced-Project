package presentation.admin;

import model.Equipment;
import service.EquipmentService;
import util.InputValidator;

import java.util.List;
import java.util.Scanner;

public class EquipmentCrudView {
    private final EquipmentService equipmentService;
    private final Scanner scanner;

    public EquipmentCrudView(EquipmentService equipmentService, Scanner scanner) {
        this.equipmentService = equipmentService;
        this.scanner = scanner;
    }

    public void showMenu() {
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
                case 1 -> printEquipmentsTable(equipmentService.getAllEquipments());
                case 2 -> createEquipment();
                case 3 -> updateEquipment();
                case 4 -> deleteEquipment();
                case 0 -> running = false;
                default -> {
                }
            }
        }
    }

    private void createEquipment() {
        String name = InputValidator.promptRequired(scanner, "Ten thiet bi: ");
        int total = InputValidator.promptIntInRange(scanner, "Tong so luong: ", 1, Integer.MAX_VALUE);
        int available = InputValidator.promptIntInRange(scanner, "So luong kha dung: ", 0, total);
        String status = readEquipmentStatus();
        equipmentService.createEquipment(name, total, available, status);
        System.out.println("Them thiet bi thanh cong.");
    }

    private void updateEquipment() {
        System.out.println("\nDanh sach thiet bi hien tai:");
        printEquipmentsTable(equipmentService.getAllEquipments());
        int equipmentId = InputValidator.promptIntInRange(scanner, "Equipment ID can cap nhat: ", 1, Integer.MAX_VALUE);
        Equipment current = equipmentService.getEquipmentById(equipmentId);
        System.out.println("\nThong tin thiet bi hien tai:");
        printEquipmentsTable(List.of(current));

        String name = InputValidator.promptOptional(scanner, "Ten thiet bi moi (bo trong de giu): ");
        if (name.isBlank()) {
            name = current.getEquipmentName();
        }

        Integer totalOpt = InputValidator.promptOptionalInt(scanner, "Tong so luong moi (bo trong de giu): ");
        int total = totalOpt == null ? current.getTotalQuantity() : totalOpt;

        Integer availableOpt = InputValidator.promptOptionalInt(scanner, "So luong kha dung moi (bo trong de giu): ");
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

    private void deleteEquipment() {
        System.out.println("\nDanh sach thiet bi hien tai:");
        printEquipmentsTable(equipmentService.getAllEquipments());
        int equipmentId = InputValidator.promptIntInRange(scanner, "Equipment ID can xoa: ", 1, Integer.MAX_VALUE);
        equipmentService.deleteEquipment(equipmentId);
        System.out.println("Xoa thiet bi thanh cong.");
    }

    private void printEquipmentsTable(List<Equipment> equipments) {
        if (equipments.isEmpty()) {
            System.out.println("Khong co du lieu thiet bi.");
            return;
        }

        String rowFormat = "%-6s %-30s %-15s %-15s %-12s%n";
        System.out.printf(rowFormat, "ID", "TEN THIET BI", "SO LUONG", "KHA DUNG", "TRANG THAI");
        System.out.println("--------------------------------------------------------------------------------");
        for (Equipment equipment : equipments) {
            System.out.printf(rowFormat,
                    equipment.getEquipmentId(),
                    truncate(equipment.getEquipmentName(), 30),
                    equipment.getTotalQuantity(),
                    equipment.getAvailableQuantity(),
                    equipment.getStatus());
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

    private String readEquipmentStatus() {
        int choice = InputValidator.promptIntInRange(scanner, "Status (1:ACTIVE, 2:MAINTENANCE, 3:INACTIVE): ", 1, 3);
        return switch (choice) {
            case 1 -> "ACTIVE";
            case 2 -> "MAINTENANCE";
            case 3 -> "INACTIVE";
            default -> throw new IllegalStateException("Gia tri status khong hop le.");
        };
    }
}
