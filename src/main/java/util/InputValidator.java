package util;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class InputValidator {
    private InputValidator() {
    }

    public static String promptRequired(Scanner scanner, String label) {
        while (true) {
            System.out.print(label);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Khong duoc de trong.");
        }
    }

    public static String promptOptional(Scanner scanner, String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }

    public static int promptIntInRange(Scanner scanner, String label, int min, int max) {
        while (true) {
            System.out.print(label);
            String raw = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(raw);
                if (value < min || value > max) {
                    System.out.println("Gia tri phai trong khoang [" + min + ", " + max + "]");
                    continue;
                }
                return value;
            } catch (NumberFormatException ex) {
                System.out.println("Vui long nhap so nguyen hop le.");
            }
        }
    }

    public static Integer promptOptionalInt(Scanner scanner, String label) {
        while (true) {
            System.out.print(label);
            String raw = scanner.nextLine().trim();
            if (raw.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException ex) {
                System.out.println("Vui long nhap so nguyen hop le hoac bo trong.");
            }
        }
    }

    public static LocalDateTime promptDateTime(Scanner scanner, String label) {
        while (true) {
            System.out.print(label + " (" + DateUtil.DATETIME_PATTERN + "): ");
            String raw = scanner.nextLine().trim();
            try {
                return DateUtil.parseDateTime(raw);
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static boolean isValidEmail(String email) {
        return email == null || email.isBlank() || email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static boolean isValidPhone(String phone) {
        return phone == null || phone.isBlank() || phone.matches("^[0-9+()\\-\\s]{6,20}$");
    }

    public static Map<Integer, Integer> parseItemQuantityMap(String input) {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        if (input == null || input.isBlank()) {
            return result;
        }

        String[] parts = input.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] pair = trimmed.split(":");
            if (pair.length != 2) {
                throw new IllegalArgumentException("Sai dinh dang. Dung: id:soLuong,id:soLuong");
            }
            int id;
            int qty;
            try {
                id = Integer.parseInt(pair[0].trim());
                qty = Integer.parseInt(pair[1].trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Id va so luong phai la so nguyen.");
            }
            if (id <= 0 || qty <= 0) {
                throw new IllegalArgumentException("Id va so luong phai lon hon 0.");
            }
            result.put(id, result.getOrDefault(id, 0) + qty);
        }
        return result;
    }
}
