package util;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
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

    public static String promptPasswordMasked(Scanner scanner, String label) {
        while (true) {
            String value = tryReadPasswordMasked(label);
            if (value == null) {
                return promptRequired(scanner, label);
            }

            value = value.trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Khong duoc de trong.");
        }
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
        return phone == null || phone.isBlank() || phone.matches("^\\d{10}$");
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

    /**
     * Thu nhap mat khau voi che do an ky tu da nhap bang dau '*'.
     * Tra ve null neu moi truong terminal khong ho tro (hoac nguoi dung huy),
     * de ham goi ben ngoai fallback ve cach nhap thong thuong.
     */
    private static String tryReadPasswordMasked(String label) {
        try (Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .providers("exec,dumb")
                .jni(false)
                .jna(false)
                .jansi(false)
                .ffm(false)
                .nativeSignals(false)
                .dumb(true)
                .build()) {
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
            // Tham so thu 2 la ky tu mask: moi ky tu mat khau nhap vao se hien thi thanh
            // '*'.
            return reader.readLine(label, '*');
        } catch (IOException | UserInterruptException | EndOfFileException ex) {
            // Cac loi I/O hoac nguoi dung nhan Ctrl+C/Ctrl+D: bao cho caller biet de
            // fallback.
            return null;
        } catch (RuntimeException ex) {
            // Bat cac loi runtime bat ngo tu terminal implementation de khong vo chuong
            // trinh.
            return null;
        }
    }
}
