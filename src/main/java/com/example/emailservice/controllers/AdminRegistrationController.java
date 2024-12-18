package com.example.emailservice.controllers;

import com.example.emailservice.entities.Admin;
import com.example.emailservice.repositories.AdminRepository;
import com.example.emailservice.services.admin.AdminRegistrationService;
import com.example.emailservice.utils.BcryptUtil;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminRegistrationController {

    private final AdminRegistrationService adminRegistrationService;
    private final AdminRepository adminRepository;

    @Autowired
    public AdminRegistrationController(AdminRegistrationService adminRegistrationService, AdminRepository adminRepository) {
        this.adminRegistrationService = adminRegistrationService;
        this.adminRepository = adminRepository;
    }

    /**
     * Authorizes the admin registration using the provided pincode.
     */
    @GetMapping("/authorize-registration/{pincode}")
    public ResponseEntity<String> authorizeRegistration(@PathVariable String pincode) {
        Optional<Admin> adminOpt = adminRepository.findByConfirmationCode(Integer.valueOf(pincode));

        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.GONE).body("Link aktywacyjny wygasł");
        }

        Admin admin = adminOpt.get();
        admin.setIsAuthorized(true);
        admin.setConfirmationCodeExpiry(null);
        admin.setConfirmationCode(null);

        try {
            adminRepository.save(admin);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Wystąpił błąd. Spróbuj ponownie później");
        }
        return ResponseEntity.ok("Autoryzacja przebiegła pomyślnie");
    }

    /**
     * Sends a password reset email to the admin.
     */
    @Transactional
    @PostMapping("/forgot-password")
    public ResponseEntity<String> sendPasswordResetEmail(@RequestBody Map<String, String> emailJson) {
        String email = emailJson.get("email");

        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            try {
                /**
                 * Possible HTTP Status Codes:
                 * - 200 OK: Link aktywacyjny został wysłany na adres:,
                 * - 409 CONFLICT: Nie można zresetować hasła. Proszę najpierw autoryzować swój profil,
                 * - 500 INTERNAL SERVER ERROR: Wystąpił błąd podczas wysyłania e-maila,
                 * - 500 INTERNAL SERVER ERROR: Wystąpił błąd podczas zapisu danych
                 */
                return adminRegistrationService.sendResetPasswordEmail(adminOpt.get());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił błąd podczas wysyłania e-maila");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Adres email nie istnieje");
        }
    }

    /**
     * Deletes the account of the currently authenticated admin.
     */
    @Transactional
    @DeleteMapping("/deleteAccount")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            adminRepository.deleteByUsername(userDetails.getUsername());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Registers a new admin.
     */
    @Transactional
    @PostMapping("/registration")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody Admin admin, BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            result.getFieldErrors().forEach(error -> response.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!admin.getPassword().equals(admin.getConPassword())) {
            response.put("conPassword", "Hasła nie pasują do siebie");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

//        if (!AdminRegistrationService.isEmailFromAllowedDomain(admin.getEmail())) {
//            response.put("email", "Email musi pochodzić z domeny @btc.com.pl");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }

        if (admin.getUsername().equals("anonymousUser")){
            response.put("username", "Ta nazwa nie jest mozliwa");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        if (adminRepository.findByUsername(admin.getUsername()).isPresent()) {
            response.put("username", "Nazwa użytkownika już istnieje");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {
            response.put("email", "Email już istnieje");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        admin.setPassword(BcryptUtil.hashPassword(admin.getPassword()));

        try {
            admin = adminRegistrationService.registerAdmin(admin);
            adminRepository.save(admin);
        } catch (DataIntegrityViolationException | MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Resets the admin's password using the provided pincode and email.
     */
    @PutMapping("/reset-password/{pincode}/{email}")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable("pincode") String pincode,
            @PathVariable("email") String email,
            @Valid @RequestBody Admin newAdmin,
            BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            result.getFieldErrors().forEach(error -> response.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!newAdmin.getPassword().equals(newAdmin.getConPassword())) {
            response.put("conPassword", "Hasła nie pasują do siebie");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<Admin> adminOpt = adminRepository.findByEmailAndConfirmationCode(email, Integer.valueOf(pincode));
        if (adminOpt.isEmpty()) {
            response.put("general", "Błędne dane, prosimy wygenerować link ponownie");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Admin admin = adminOpt.get();
        if (admin.getConfirmationCodeExpiry().isBefore(LocalDateTime.now())) {
            response.put("general", "Data ważności linku minęła, prosimy wygenerować link ponownie");
            return ResponseEntity.status(HttpStatus.GONE).body(response);
        }

        admin.setPassword(BcryptUtil.hashPassword(newAdmin.getPassword()));
        admin.setConfirmationCode(null);
        admin.setConfirmationCodeExpiry(null);

        try {
            adminRepository.save(admin);
        } catch (DataIntegrityViolationException e) {
            response.put("general", "Wystąpił błąd w zapisie danych, prosimy spróbować ponownie później");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Verifies the reset password details using the provided pincode and email.
     */
    @GetMapping("/verify-reset/{pincode}/{email}")
    public ResponseEntity<String> verifyResetDetails(
            @PathVariable("pincode") String pincode,
            @PathVariable("email") String email) {

        Optional<Admin> adminOpt = adminRepository.findByEmailAndConfirmationCode(email, Integer.valueOf(pincode));
        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Błędne dane, prosimy wygenerować link ponownie");
        }

        Admin admin = adminOpt.get();
        if (admin.getConfirmationCodeExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("Data ważności linku minęła, prosimy wygenerować go ponownie");
        }
        return ResponseEntity.ok().build();
    }
}
