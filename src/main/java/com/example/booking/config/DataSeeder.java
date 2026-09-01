package com.example.booking.config;

import com.example.booking.entity.AppUser;
import com.example.booking.entity.BookableResource;
import com.example.booking.entity.Reservation;
import com.example.booking.entity.ReservationStatus;
import com.example.booking.entity.Role;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               ResourceRepository resourceRepository,
                               ReservationRepository reservationRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new AppUser("admin", passwordEncoder.encode("Admin@123"), Role.ADMIN));
            }

            if (!userRepository.existsByUsername("user")) {
                userRepository.save(new AppUser("user", passwordEncoder.encode("User@123"), Role.USER));
            }

            if (!userRepository.existsByUsername("user2")) {
                userRepository.save(new AppUser("user2", passwordEncoder.encode("User2@123"), Role.USER));
            }

            if (resourceRepository.count() == 0) {
                BookableResource conferenceRoom = resourceRepository.save(new BookableResource(
                        "Conference Room A",
                        "ROOM",
                        "Large meeting room with projector and video conferencing.",
                        true,
                        new BigDecimal("50.00")
                ));

                resourceRepository.save(new BookableResource(
                        "Delivery Van",
                        "VEHICLE",
                        "Cargo van for local deliveries.",
                        true,
                        new BigDecimal("35.00")
                ));

                resourceRepository.save(new BookableResource(
                        "Camera Kit",
                        "EQUIPMENT",
                        "Mirrorless camera, tripod, and microphone.",
                        true,
                        new BigDecimal("20.00")
                ));

                AppUser user = userRepository.findByUsername("user").orElseThrow();

                reservationRepository.save(new Reservation(
                        conferenceRoom,
                        user,
                        OffsetDateTime.now().plusDays(2),
                        OffsetDateTime.now().plusDays(2).plusHours(2),
                        ReservationStatus.CONFIRMED,
                        new BigDecimal("100.00")
                ));
            }
        };
    }
}