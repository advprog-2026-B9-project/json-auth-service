package com.b9.json.jsonplatform.auth.application.service;

import com.b9.json.jsonplatform.auth.domain.KycStatus;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.domain.UserRole;
import com.b9.json.jsonplatform.auth.infrastructure.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final RestTemplate restTemplate;

    @Value("${app.wallet-service.base-url}")
    private String walletServiceBaseUrl;

    @Value("${app.order-service.base-url}")
    private String orderServiceBaseUrl;

    public AuthServiceImpl(UserRepository userRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    private String resolveUsername(String requestedUsername, String email) {
        if (email == null) {
            return (requestedUsername != null && !requestedUsername.trim().isEmpty())
                    ? requestedUsername
                    : "user_tanpa_email";
        }

        if (requestedUsername == null || requestedUsername.trim().isEmpty()) {
            return email.split("@")[0];
        }
        return requestedUsername;
    }

    @Override
    @Transactional
    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email sudah digunakan");
        }

        user.setUsername(resolveUsername(user.getUsername(), user.getEmail()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        try {
            String walletServiceUrl = walletServiceBaseUrl + "/wallets/users/" + savedUser.getId();

            restTemplate.postForObject(walletServiceUrl, null, String.class);
            log.info("Berhasil request pembuatan wallet ke Wallet-Service");
        }
        catch (Exception e) {
            log.error("Gagal memanggil Wallet Service: {}", e.getMessage());
        }

        return savedUser;
    }

    @Override
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public User updateProfile(String email, User updatedUser) {
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            existingUser.setFullName(updatedUser.getFullName());
            existingUser.setUsername(resolveUsername(updatedUser.getUsername(), existingUser.getEmail()));
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            existingUser.setAddress(updatedUser.getAddress());
            return userRepository.save(existingUser);
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<User> findAllUsers(String status) {
        List<User> all = userRepository.findAll();
        if (status == null) return all;
        return switch (status.toLowerCase()) {
            case "active"  -> all.stream()
                    .filter(u -> !u.isBanned())
                    .filter(u -> !KycStatus.PENDING_VERIFICATION.equals(u.getKycStatus()))
                    .toList();
            case "banned"  -> all.stream()
                    .filter(User::isBanned)
                    .toList();
            case "pending" -> all.stream()
                    .filter(u -> KycStatus.PENDING_VERIFICATION.equals(u.getKycStatus()))
                    .toList();
            default -> throw new IllegalArgumentException(
                    "Status tidak valid: " + status + ". Gunakan: active, banned, pending");
        };
    }

    @Override
    public User demoteJastiper(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null && UserRole.JASTIPER.equals(user.getRole())) {
            user.setRole(UserRole.TITIPERS);
            user.setKycStatus(KycStatus.UNVERIFIED);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User banUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setBanned(true);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public long countSuccessfulTransactions(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return 0;

        try {
            String orderServiceUrl = orderServiceBaseUrl + "/api/orders/jastiper/" + user.getId() + "/stats";
            Long count = restTemplate.getForObject(orderServiceUrl, Long.class);

            return count != null ? count : 0;
        }
        catch (Exception e) {
            log.error("Gagal mengambil data dari Order Service: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    @Transactional
    public User addRating(String email, int ratingScore) {
        if (ratingScore < 1 || ratingScore > 5) {
            throw new IllegalArgumentException("Rating harus antara 1 dan 5");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User tidak ditemukan");
        }

        int currentReviews = user.getTotalReviews();
        double currentRating = user.getRating();
        double newRating = ((currentRating * currentReviews) + ratingScore) / (currentReviews + 1);

        user.setRating(Math.round(newRating * 100.0) / 100.0);
        user.setTotalReviews(currentReviews + 1);

        return userRepository.save(user);
    }
}