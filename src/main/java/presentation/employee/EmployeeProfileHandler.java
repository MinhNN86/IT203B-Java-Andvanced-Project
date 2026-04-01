package presentation.employee;

import model.User;
import service.AuthService;
import util.InputValidator;

import java.util.Scanner;

public class EmployeeProfileHandler {
    private final AuthService authService;
    private final Scanner scanner;

    public EmployeeProfileHandler(AuthService authService, Scanner scanner) {
        this.authService = authService;
        this.scanner = scanner;
    }

    public User handleProfile(User currentUser) {
        System.out.println("\n--- HO SO HIEN TAI ---");
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("Ho ten  : " + currentUser.getFullName());
        System.out.println("Email   : " + valueOrDash(currentUser.getEmail()));
        System.out.println("Phone   : " + valueOrDash(currentUser.getPhone()));

        int choice = InputValidator.promptIntInRange(scanner, "Cap nhat ho so? (1:Co, 0:Khong): ", 0, 1);
        if (choice == 0) {
            return currentUser;
        }

        String fullNameInput = InputValidator.promptOptional(scanner, "Ho ten moi (bo trong de giu): ");
        String emailInput = InputValidator.promptOptional(scanner, "Email moi (bo trong de giu): ");
        String phoneInput = InputValidator.promptOptional(scanner, "Phone moi (bo trong de giu): ");

        String fullName = fullNameInput.isBlank() ? currentUser.getFullName() : fullNameInput;
        String email = emailInput.isBlank() ? currentUser.getEmail() : emailInput;
        String phone = phoneInput.isBlank() ? currentUser.getPhone() : phoneInput;

        User updated = authService.updateProfile(currentUser, fullName, email, phone);
        System.out.println("Cap nhat ho so thanh cong.");
        return updated;
    }

    private String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
}
