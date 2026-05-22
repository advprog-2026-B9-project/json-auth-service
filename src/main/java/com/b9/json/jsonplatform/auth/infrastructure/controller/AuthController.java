package com.b9.json.jsonplatform.auth.infrastructure.controller;

import com.b9.json.jsonplatform.auth.application.dto.*;
import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.application.service.KycService;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.domain.UserRole;
import com.b9.json.jsonplatform.auth.infrastructure.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final String FORBIDDEN_ADMIN_MESSAGE = "Akses ditolak. Hanya Admin yang dapat mengakses fitur ini.";

    private final AuthService authService;
    private final KycService kycService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, KycService kycService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.kycService = kycService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody RegisterRequest request) {
        try {
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setUsername(request.getUsername());
            user.setFullName(request.getFullName());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setAddress(request.getAddress());

            User savedUser = authService.registerUser(user);
            RegisterResponse response = new RegisterResponse(
                    savedUser.getId().toString(),
                    savedUser.getEmail(),
                    savedUser.getUsername(),
                    savedUser.getRole().name(),
                    "Registrasi berhasil!"
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> loginUser(@RequestBody LoginRequest loginData) {
        User loggedInUser = authService.loginUser(loginData.getEmail(), loginData.getPassword());
        if (loggedInUser != null) {
            String token = jwtUtil.generateToken(
                    loggedInUser.getEmail(),
                    loggedInUser.getRole().name(),
                    loggedInUser.getId().toString()
            );

            LoginResponse response = new LoginResponse(
                    loggedInUser.getId().toString(),
                    token,
                    loggedInUser.getEmail(),
                    loggedInUser.getUsername(),
                    loggedInUser.getRole().name()
            );

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body("Email atau password salah!");
    }

    @PutMapping("/profile")
    public ResponseEntity<Object> updateProfile(@RequestParam String email, @RequestBody UpdateProfileRequest request) {
        User updatedUserData = new User();
        updatedUserData.setFullName(request.getFullName());
        updatedUserData.setUsername(request.getUsername());
        updatedUserData.setPhoneNumber(request.getPhoneNumber());
        updatedUserData.setAddress(request.getAddress());

        User savedUser = authService.updateProfile(email, updatedUserData);
        if (savedUser != null) {
            return ResponseEntity.ok(savedUser);
        }
        return ResponseEntity.badRequest().body("User tidak ditemukan!");
    }

    @GetMapping("/list")
    public ResponseEntity<Object> listUsers(@RequestParam(required = false) String status) {
        try {
            return ResponseEntity.ok(authService.findAllUsers(status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<Object> getUserByEmail(@RequestParam String email) {
        User user = authService.findByEmail(email);
        if (user != null) {
            PublicProfileResponse response = new PublicProfileResponse();
            response.setUsername(user.getUsername());
            response.setFullName(user.getFullName());
            response.setEmail(user.getEmail());
            response.setRole(user.getRole().name());
            response.setKycStatus(user.getKycStatus().name());
            response.setBanned(user.isBanned());
            response.setRating(user.getRating());
            response.setTotalReviews(user.getTotalReviews());

            if (UserRole.JASTIPER.equals(user.getRole())) {
                response.setTotalSuccessfulTransactions(
                        authService.countSuccessfulTransactions(email)
                );
            } else {
                response.setTotalSuccessfulTransactions(0);
            }
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body("User tidak ditemukan!");
    }


    @PostMapping("/kyc/submit")
    public ResponseEntity<Object> submitKyc(@RequestBody KycRequest request) {
        if (request.getNikKtp() == null || request.getNikKtp().isBlank()) {
            return ResponseEntity.badRequest().body("NIK KTP tidak boleh kosong");
        }

        User updatedUser = kycService.submitKyc(
                request.getEmail(),
                request.getFullName(),
                request.getNikKtp(),
                request.getKtpImageUrl()
        );

        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.badRequest().body("User dengan email tersebut tidak ditemukan");
    }

    private boolean isNotAdmin(String requesterEmail) {
        User requester = authService.findByEmail(requesterEmail);
        return requester == null || !UserRole.ADMIN.equals(requester.getRole());
    }

    @GetMapping("/admin/kyc/pending")
    public ResponseEntity<Object> getPendingKyc(@RequestParam String requesterEmail) {
        if (isNotAdmin(requesterEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FORBIDDEN_ADMIN_MESSAGE);
        }
        return ResponseEntity.ok(kycService.findPendingKyc());
    }

    @PostMapping("/admin/kyc/review")
    public ResponseEntity<Object> reviewKyc(
            @RequestParam String requesterEmail,
            @RequestBody KycReviewRequest request) {
        if (isNotAdmin(requesterEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FORBIDDEN_ADMIN_MESSAGE);
        }

        User result = kycService.reviewKyc(request.getEmail(), request.isApproved());
        if (result != null) {
            String message = request.isApproved() ?
                    "KYC Disetujui. Akun berhasil di-upgrade menjadi JASTIPER." :
                    "KYC Ditolak.";
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.badRequest().body("Gagal melakukan review. Pastikan statusnya PENDING_VERIFICATION.");
    }

    @PostMapping("/admin/demote")
    public ResponseEntity<Object> demoteUser(
            @RequestParam String requesterEmail,
            @RequestParam String email) {
        if (isNotAdmin(requesterEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FORBIDDEN_ADMIN_MESSAGE);
        }

        User result = authService.demoteJastiper(email);
        if (result != null) {
            return ResponseEntity.ok("User berhasil di-demote menjadi TITIPERS.");
        }
        return ResponseEntity.badRequest().body("Gagal demote. Pastikan user adalah JASTIPER.");
    }

    @PostMapping("/admin/ban")
    public ResponseEntity<Object> banUser(
            @RequestParam String requesterEmail,
            @RequestParam String email) {
        if (isNotAdmin(requesterEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FORBIDDEN_ADMIN_MESSAGE);
        }

        User result = authService.banUser(email);
        if (result != null) {
            return ResponseEntity.ok("User berhasil di-banned.");
        }
        return ResponseEntity.badRequest().body("Gagal melakukan banned.");
    }

    @PostMapping("/rating")
    public ResponseEntity<Object> addRating(
            @RequestParam String jastiperEmail,
            @RequestParam int ratingScore) {
        try {
            User updated = authService.addRating(jastiperEmail, ratingScore);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/internal/user")
    public ResponseEntity<Object> getUserById(@RequestParam UUID id) {
        User user = authService.findById(id);
        if (user != null) {
            return ResponseEntity.ok(new UserInternalResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getPhoneNumber()
            ));
        }
        return ResponseEntity.notFound().build();
    }
}