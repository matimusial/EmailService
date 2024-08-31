package com.example.emailservice.services.admin;

import com.example.emailservice.entities.Admin;
import com.example.emailservice.repositories.AdminRepository;
import com.example.emailservice.services.EmailNotificationService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AdminRegistrationService {

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    private final EmailNotificationService emailNotificationService;
    private final AdminRepository adminRepository;

    public AdminRegistrationService(EmailNotificationService emailNotificationService, AdminRepository adminRepository) {
        this.emailNotificationService = emailNotificationService;
        this.adminRepository = adminRepository;
    }

    /**
     * Registers a new admin by generating a confirmation code and sending a confirmation email.
     */
    @Transactional
    public Admin registerAdmin(Admin admin) throws MessagingException {
        Integer pinCode = generateSixDigitCode();
        admin.setConfirmationCodeExpiry(generateExpiryDate(24));
        admin.setConfirmationCode(pinCode);
        admin.setFirstName(capitalizeFirstLetter(admin.getFirstName()));
        String confirmationUrl = generateConfirmationUrl(pinCode);

        emailNotificationService.sendRegistrationConfirmationEmail(admin.getEmail(), confirmationUrl, admin.getFirstName());
        return admin;
    }

    /**
     * Sends a password reset email to the admin if they are authorized.
     */
    @Transactional
    public ResponseEntity<String> sendResetPasswordEmail(Admin admin) {
        if (!admin.getIsAuthorized()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nie można zresetować hasła. Proszę najpierw autoryzować swój profil");
        }

        Integer pinCode = generateSixDigitCode();
        admin.setConfirmationCode(pinCode);
        admin.setConfirmationCodeExpiry(generateExpiryDate(1));
        String resetPasswordUrl = generateResetPasswordUrl(pinCode, admin.getEmail());

        try {
            adminRepository.save(admin);
            emailNotificationService.sendPasswordResetEmail(admin.getEmail(), resetPasswordUrl, admin.getFirstName());
            return ResponseEntity.ok("Link aktywacyjny został wysłany na adres: " + admin.getEmail());
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił błąd podczas wysłania e-maila");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Wystąpił błąd podczas zapisu danych");
        }
    }

    private String generateConfirmationUrl(Integer pinCode) {
        return frontendBaseUrl + "/admin/registration/authorize-registration?pincode=" + pinCode;
    }

    private String generateResetPasswordUrl(Integer pinCode, String email) {
        return frontendBaseUrl + "/admin/login/reset-password?pincode=" + pinCode + "&email=" + email;
    }

    private int generateSixDigitCode() {
        return ThreadLocalRandom.current().nextInt(100000, 1000000);
    }

    private LocalDateTime generateExpiryDate(Integer hours) {
        return LocalDateTime.now().plusHours(hours);
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public static boolean isEmailFromAllowedDomain(String email) {
        return email.endsWith("@btc.com.pl");
    }
}
