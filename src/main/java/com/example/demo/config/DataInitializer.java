package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * DataInitializer - Dijalankan otomatis saat aplikasi pertama kali startup.
 * Membuat akun admin default jika belum ada di database MySQL.
 *
 * Username : admin
 * Password : admin123
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        Optional<User> existingAdmin = userRepository.findByUsername("admin");

        if (existingAdmin.isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRole("ADMIN");
            admin.setCaptainName("Administrator");
            admin.setContact("admin@bracktix.com");

            userRepository.save(admin);

            System.out.println("====================================================");
            System.out.println("  [BRACKTIX] Akun Admin berhasil dibuat!");
            System.out.println("  Username : admin");
            System.out.println("  Password : admin123");
            System.out.println("====================================================");
        } else {
            System.out.println("[BRACKTIX] Akun Admin sudah ada di database.");
        }
    }
}
