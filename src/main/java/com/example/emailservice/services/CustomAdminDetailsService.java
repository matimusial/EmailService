package com.example.emailservice.services;

import com.example.emailservice.entities.Admin;
import com.example.emailservice.repositories.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomAdminDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Autowired
    public CustomAdminDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    /**
     * Loads the admin user by username and checks if the user is authorized.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);

        if (adminOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        Admin admin = adminOpt.get();
        if (!admin.getIsAuthorized()) {
            throw new UsernameNotFoundException("User is not authorized");
        }

        return org.springframework.security.core.userdetails.User.withUsername(admin.getUsername())
                .password(admin.getPassword())
                .build();
    }
}
