package com.b9.json.jsonplatform.auth.infrastructure.controller;

import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.application.dto.UserResponseDto;
import com.b9.json.jsonplatform.auth.application.dto.KycRequest;
import com.b9.json.jsonplatform.auth.application.dto.KycReviewRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User savedUser = authService.registerUser(user);
            return ResponseEntity.ok(new UserResponseDto(savedUser));
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginData) {
        User loggedInUser = authService.loginUser(loginData.getEmail(), loginData.getPassword());
        if (loggedInUser != null) {
            return ResponseEntity.ok(new UserResponseDto(loggedInUser));
        }
        return ResponseEntity.badRequest().body("Email atau password salah!");
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestParam String email, @RequestBody User updatedUser) {
        User savedUser = authService.updateProfile(email, updatedUser);
        if (savedUser != null) {
            return ResponseEntity.ok(new UserResponseDto(savedUser));
        }
        return ResponseEntity.badRequest().body("User tidak ditemukan!");
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserResponseDto>> listUsers() {
        return ResponseEntity.ok(
                authService.findAllUsers().stream()
                        .map(UserResponseDto::new)
                        .toList()
        );
    }

    @PostMapping("/kyc/submit")
    public ResponseEntity<?> submitKyc(@RequestBody KycRequest request) {
        if (request.getNikKtp() == null || request.getNikKtp().isBlank()) {
            return ResponseEntity.badRequest().body("NIK KTP tidak boleh kosong");
        }
        User updatedUser = authService.submitKyc(
                request.getEmail(),
                request.getFullName(),
                request.getNikKtp(),
                request.getKtpImageUrl()
        );
        if (updatedUser != null) {
            return ResponseEntity.ok(new UserResponseDto(updatedUser));
        }
        return ResponseEntity.badRequest().body("User dengan email tersebut tidak ditemukan");
    }

    @GetMapping("/admin/kyc/pending")
    public ResponseEntity<?> getPendingKyc(@RequestParam String adminEmail) {
        try {
            authService.validateAdmin(adminEmail);
            return ResponseEntity.ok(
                    authService.findPendingKyc().stream()
                            .map(UserResponseDto::new)
                            .toList()
            );
        }
        catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PostMapping("/admin/kyc/review")
    public ResponseEntity<?> reviewKyc(@RequestBody KycReviewRequest request) {
        try {
            authService.validateAdmin(request.getAdminEmail());
            User result = authService.reviewKyc(request.getEmail(), request.isApproved());
            if (result != null) {
                String message = request.isApproved() ?
                        "KYC Disetujui. Akun berhasil di-upgrade menjadi JASTIPER." :
                        "KYC Ditolak.";
                return ResponseEntity.ok(message);
            }
            return ResponseEntity.badRequest().body("Gagal melakukan review. Pastikan email benar dan statusnya PENDING_VERIFICATION.");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}