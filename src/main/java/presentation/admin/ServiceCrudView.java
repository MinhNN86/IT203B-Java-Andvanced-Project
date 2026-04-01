package presentation.admin;

import model.Service;
import service.BookingService;
import util.InputValidator;

import java.util.List;
import java.util.Scanner;

public class ServiceCrudView {
    private final BookingService bookingService;
    private final Scanner scanner;

    public ServiceCrudView(BookingService bookingService, Scanner scanner) {
        this.bookingService = bookingService;
        this.scanner = scanner;
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- QUAN LY DICH VU ---");
            System.out.println("1. Xem danh sach");
            System.out.println("2. Them moi");
            System.out.println("3. Cap nhat");
            System.out.println("4. Xoa");
            System.out.println("0. Quay lai");

            int choice = InputValidator.promptIntInRange(scanner, "Chon: ", 0, 4);
            switch (choice) {
                case 1 -> printServicesTable(bookingService.getAllServices());
                case 2 -> createService();
                case 3 -> updateService();
                case 4 -> deleteService();
                case 0 -> running = false;
                default -> {
                }
            }
        }
    }

    private void createService() {
        String name = InputValidator.promptRequired(scanner, "Ten dich vu: ");
        double price = promptDouble("Don gia: ");
        String unit = InputValidator.promptOptional(scanner, "Don vi tinh: ");
        bookingService.createService(name, price, unit);
        System.out.println("Them dich vu thanh cong.");
    }

    private void updateService() {
        System.out.println("\nDanh sach dich vu hien tai:");
        printServicesTable(bookingService.getAllServices());
        int serviceId = InputValidator.promptIntInRange(scanner, "Service ID can cap nhat: ", 1, Integer.MAX_VALUE);
        List<Service> services = bookingService.getAllServices();
        Service current = services.stream()
                .filter(service -> service.getServiceId() == serviceId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay dich vu."));
        System.out.println("\nThong tin dich vu hien tai:");
        printServicesTable(List.of(current));

        String name = InputValidator.promptOptional(scanner, "Ten dich vu moi (bo trong de giu): ");
        if (name.isBlank()) {
            name = current.getServiceName();
        }

        String priceRaw = InputValidator.promptOptional(scanner, "Don gia moi (bo trong de giu): ");
        double price = priceRaw.isBlank() ? current.getPrice()
                : parseDoubleStrict(priceRaw, "Don gia khong hop le.");

        String unit = InputValidator.promptOptional(scanner, "Don vi tinh moi (bo trong de giu): ");
        if (unit.isBlank()) {
            unit = current.getUnit();
        }

        bookingService.updateService(serviceId, name, price, unit);
        System.out.println("Cap nhat dich vu thanh cong.");
    }

    private void deleteService() {
        System.out.println("\nDanh sach dich vu hien tai:");
        printServicesTable(bookingService.getAllServices());
        int serviceId = InputValidator.promptIntInRange(scanner, "Service ID can xoa: ", 1, Integer.MAX_VALUE);
        bookingService.deleteService(serviceId);
        System.out.println("Xoa dich vu thanh cong.");
    }

    private void printServicesTable(List<Service> services) {
        if (services.isEmpty()) {
            System.out.println("Khong co du lieu dich vu.");
            return;
        }

        String rowFormat = "%-6s %-35s %-15s %-15s%n";
        System.out.printf(rowFormat, "ID", "TEN DICH VU", "DON GIA", "DON VI TINH");
        System.out.println("-------------------------------------------------------------------");
        for (Service service : services) {
            System.out.printf(rowFormat,
                    service.getServiceId(),
                    truncate(service.getServiceName(), 35),
                    String.format("%.2f", service.getPrice()),
                    truncate(valueOrDash(service.getUnit()), 15));
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

    private double promptDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Gia tri khong hop le. Vui long nhap lai.");
            }
        }
    }

    private double parseDoubleStrict(String value, String errorMessage) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
