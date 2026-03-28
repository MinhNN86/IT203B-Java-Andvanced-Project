package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class SecurityUtil {
    
    // Không cho phép tạo instance
    private SecurityUtil() {
    }

    /**
     * Mã hóa mật khẩu bằng thuật toán SHA-256
     * @param password Mật khẩu cần mã hóa
     * @return Chuỗi mật khẩu đã mã hóa (hex string)
     */
    public static String hashPassword(String password) {
        // Kiểm tra mật khẩu không được rỗng
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Mat khau khong duoc de trong.");
        }

        try {
            // Tạo bộ mã hóa SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Chuyển mật khẩu thành bytes và mã hóa
            byte[] hashBytes = digest.digest(password.getBytes());
            
            // Chuyển bytes thành chuỗi hex
            return HexFormat.of().formatHex(hashBytes);
            
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Loi khi ma hoa mat khau: " + ex.getMessage(), ex);
        }
    }

    /**
     * Kiểm tra mật khẩu có khớp với mật khẩu đã mã hóa không
     * @param password Mật khẩu cần kiểm tra
     * @param hashedPassword Mật khẩu đã mã hóa từ database
     * @return true nếu khớp, false nếu không khớp
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        // Kiểm tra đầu vào không được null
        if (password == null || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }
        
        // Mã hóa mật khẩu và so sánh với mật khẩu đã lưu
        String computedHash = hashPassword(password);
        return computedHash.equals(hashedPassword);
    }
}
