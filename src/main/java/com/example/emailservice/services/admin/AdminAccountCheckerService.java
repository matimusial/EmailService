package com.example.emailservice.services.admin;

import com.example.emailservice.entities.Admin;
import com.example.emailservice.repositories.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminAccountCheckerService {

    private final AdminRepository adminRepository;

    @Autowired
    public AdminAccountCheckerService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    /**
     * Scheduled task that checks for unauthorized admin accounts with expired confirmation codes
     * and removes them from the database.
     */
    @Scheduled(fixedRate = 21600000) // 6 hours
    @Transactional
    public void checkAdminExpiryStatus() {
        LocalDateTime currentTime = LocalDateTime.now();
        List<Admin> expiredAdmins = adminRepository.findByIsAuthorizedFalseAndConfirmationCodeExpiryBefore(currentTime);

        for (Admin admin : expiredAdmins) {
            adminRepository.deleteByUsername(admin.getUsername());
            System.out.println("Deleted expired admin with username: " + admin.getUsername());
        }
    }
}
