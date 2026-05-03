package com.b9.json.jsonplatform.auth.application.service;

import com.b9.json.jsonplatform.auth.domain.KycStatus;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.domain.UserRole;
import com.b9.json.jsonplatform.auth.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
            throw new IllegalArgumentException("Email sudah terdaftar");
        }

        user.setUsername(resolveUsername(user.getUsername(), user.getEmail()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
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
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User submitKyc(String email, String fullName, String nikKtp, String ktpImageUrl) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            if (user.getKycStatus() == KycStatus.PENDING_VERIFICATION) {
                throw new IllegalStateException("KYC sudah diajukan, sedang menunggu review");
            }
            if (user.getKycStatus() == KycStatus.VERIFIED) {
                throw new IllegalStateException("Akun sudah terverifikasi sebagai Jastiper");
            }

            user.setFullName(fullName);
            user.setNikKtp(nikKtp);
            user.setKtpImageUrl(ktpImageUrl);
            user.setKycStatus(KycStatus.PENDING_VERIFICATION);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public List<User> findPendingKyc() {
        return userRepository.findAll().stream()
                .filter(u -> KycStatus.PENDING_VERIFICATION == u.getKycStatus())
                .toList();
    }

    @Override
    @Transactional
    public User reviewKyc(String email, boolean approved) {
        User user = userRepository.findByEmail(email);
        if (user != null && KycStatus.PENDING_VERIFICATION == user.getKycStatus()){
            if (approved) {
                user.setKycStatus(KycStatus.VERIFIED);
                user.setRole(UserRole.JASTIPER);
            } else {
                user.setKycStatus(KycStatus.UNVERIFIED);
            }
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}