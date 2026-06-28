package com.b9.json.jsonplatform.auth.infrastructure.controller;

import com.b9.json.jsonplatform.auth.application.dto.KycRequest;
import com.b9.json.jsonplatform.auth.application.dto.KycReviewRequest;
import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.application.service.KycService;
import com.b9.json.jsonplatform.auth.domain.KycStatus;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.domain.UserRole;
import com.b9.json.jsonplatform.auth.infrastructure.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.hamcrest.Matchers.containsString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private KycService kycService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // --- REGISTER & LOGIN TESTS ---

    @Test
    void testRegisterUser() throws Exception {
        User user = new User();
        user.setId(java.util.UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setUsername("customUser");
        user.setRole(UserRole.TITIPERS);

        Mockito.when(authService.registerUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("customUser"));
    }

    @Test
    void testRegisterUserFailure() throws Exception {
        User user = new User();
        Mockito.when(authService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Email sudah digunakan"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email sudah digunakan"));
    }

    @Test
    void testLoginUserSuccess() throws Exception {
        User user = new User();
        user.setId(java.util.UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setRole(UserRole.TITIPERS);

        Mockito.when(authService.loginUser("test@example.com", "password123")).thenReturn(user);
        Mockito.when(jwtUtil.generateToken(anyString(), anyString(), anyString())).thenReturn("dummy-token");

        User loginData = new User();
        loginData.setEmail("test@example.com");
        loginData.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testLoginUserFailure() throws Exception {
        Mockito.when(authService.loginUser("test@example.com", "wrong")).thenReturn(null);

        User loginData = new User();
        loginData.setEmail("test@example.com");
        loginData.setPassword("wrong");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email atau password salah!"));
    }

    // --- PROFILE & USER SEARCH TESTS ---

    @Test
    void testUpdateProfileSuccess() throws Exception {
        User updatedUser = new User();
        updatedUser.setFullName("New Name");
        Mockito.when(authService.updateProfile(eq("test@example.com"), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/auth/profile")
                        .param("email", "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("New Name"));
    }

    @Test
    void testUpdateProfileFailure() throws Exception {
        Mockito.when(authService.updateProfile(anyString(), any(User.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/auth/profile")
                        .param("email", "ghost@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new User())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User tidak ditemukan!"));
    }

    @Test
    void testGetUserByEmailSuccess() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setRole(UserRole.JASTIPER);
        user.setKycStatus(KycStatus.VERIFIED);

        Mockito.when(authService.findByEmail("test@example.com")).thenReturn(user);
        Mockito.when(authService.countSuccessfulTransactions("test@example.com")).thenReturn(5L);

        mockMvc.perform(get("/api/v1/auth/user")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("JASTIPER"))
                .andExpect(jsonPath("$.totalSuccessfulTransactions").value(5));
    }

    @Test
    void testGetUserByEmailNotFound() throws Exception {
        Mockito.when(authService.findByEmail("ghost@example.com")).thenReturn(null);

        mockMvc.perform(get("/api/v1/auth/user")
                        .param("email", "ghost@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User tidak ditemukan!"));
    }

    @Test
    void testGetUserByEmailNonJastiper() throws Exception {
        User user = new User();
        user.setEmail("titipers@example.com");
        user.setUsername("titipers");
        user.setRole(UserRole.TITIPERS); // Skenario Bukan JASTIPER
        user.setKycStatus(KycStatus.UNVERIFIED);

        Mockito.when(authService.findByEmail("titipers@example.com")).thenReturn(user);

        mockMvc.perform(get("/api/v1/auth/user")
                        .param("email", "titipers@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("TITIPERS"))
                .andExpect(jsonPath("$.totalSuccessfulTransactions").value(0));
    }

    @Test
    void testGetUserByIdSuccess() throws Exception {
        UUID userId = java.util.UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setFullName("Test User");
        user.setPhoneNumber("08123456789");

        Mockito.when(authService.findById(userId)).thenReturn(user);

        mockMvc.perform(get("/api/v1/auth/internal/user")
                        .param("id", userId.toString())) // Parameter sekarang "id"
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString())) // Verifikasi field id baru
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.phoneNumber").value("08123456789"));
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        UUID randomId = java.util.UUID.randomUUID();
        Mockito.when(authService.findById(randomId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/auth/internal/user")
                        .param("id", randomId.toString()))
                .andExpect(status().isNotFound());
    }

    // --- LIST USERS TESTS ---

    @Test
    void testListUsersSuccess() throws Exception {
        Mockito.when(authService.findAllUsers(any())).thenReturn(java.util.List.of(new User()));

        mockMvc.perform(get("/api/v1/auth/list"))
                .andExpect(status().isOk());
    }

    @Test
    void testListUsersFailure() throws Exception {
        Mockito.when(authService.findAllUsers(any()))
                .thenThrow(new IllegalArgumentException("Status tidak valid"));

        mockMvc.perform(get("/api/v1/auth/list")
                        .param("status", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Status tidak valid"));
    }

    // --- KYC TESTS ---

    @Test
    void testSubmitKycSuccess() throws Exception {
        User user = new User();
        user.setKycStatus(KycStatus.PENDING_VERIFICATION);

        Mockito.when(kycService.submitKyc(any(), any(), any(), any())).thenReturn(user);

        KycRequest request = new KycRequest();
        request.setEmail("test@example.com");
        request.setNikKtp("1234567890123456");
        request.setFullName("Budi Santoso");
        request.setKtpImageUrl("http://image.com/ktp.jpg");

        mockMvc.perform(post("/api/v1/auth/kyc/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testSubmitKycBlankNik() throws Exception {
        KycRequest request = new KycRequest();
        request.setEmail("test@example.com");
        request.setNikKtp("");

        mockMvc.perform(post("/api/v1/auth/kyc/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("NIK KTP tidak boleh kosong"));
    }

    @Test
    void testSubmitKycUserNotFound() throws Exception {
        Mockito.when(kycService.submitKyc(any(), any(), any(), any())).thenReturn(null);

        KycRequest request = new KycRequest();
        request.setEmail("ghost@example.com");
        request.setNikKtp("1234567890123456");

        mockMvc.perform(post("/api/v1/auth/kyc/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User dengan email tersebut tidak ditemukan"));
    }

    @Test
    void testGetPendingKycSuccess() throws Exception {
        Mockito.when(kycService.findPendingKyc()).thenReturn(java.util.List.of(new User()));

        mockMvc.perform(get("/api/v1/auth/admin/kyc/pending"))
                .andExpect(status().isOk());
    }

    @Test
    void testReviewKycRejected() throws Exception {
        Mockito.when(kycService.reviewKyc("user@example.com", false)).thenReturn(new User());

        KycReviewRequest request = new KycReviewRequest();
        request.setEmail("user@example.com");
        request.setApproved(false);

        mockMvc.perform(post("/api/v1/auth/admin/kyc/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("KYC Ditolak")));
    }

    @Test
    void testReviewKycFailure() throws Exception {
        Mockito.when(kycService.reviewKyc(anyString(), anyBoolean())).thenReturn(null);

        KycReviewRequest request = new KycReviewRequest();
        request.setEmail("user@example.com");
        request.setApproved(true);

        mockMvc.perform(post("/api/v1/auth/admin/kyc/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Gagal melakukan review")));
    }

    @Test
    void testSubmitKycNullNik() throws Exception {
        KycRequest request = new KycRequest();
        request.setEmail("test@example.com");
        request.setNikKtp(null);

        mockMvc.perform(post("/api/v1/auth/kyc/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("NIK KTP tidak boleh kosong"));
    }

    // --- ADMIN TESTS ---

    @Test
    void testReviewKycSuccess() throws Exception {
        Mockito.when(kycService.reviewKyc("user@example.com", true)).thenReturn(new User());

        KycReviewRequest request = new KycReviewRequest();
        request.setEmail("user@example.com");
        request.setApproved(true);

        mockMvc.perform(post("/api/v1/auth/admin/kyc/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("KYC Disetujui")));
    }

    @Test
    void testDemoteUserSuccess() throws Exception {
        Mockito.when(authService.demoteJastiper("jastiper@example.com")).thenReturn(new User());

        mockMvc.perform(post("/api/v1/auth/admin/demote")
                        .param("email", "jastiper@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    void testBanUserSuccess() throws Exception {
        Mockito.when(authService.banUser("user@example.com")).thenReturn(new User());

        mockMvc.perform(post("/api/v1/auth/admin/ban")
                        .param("email", "user@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    void testDemoteUserFailure() throws Exception {
        Mockito.when(authService.demoteJastiper("ghost@example.com")).thenReturn(null);

        mockMvc.perform(post("/api/v1/auth/admin/demote")
                        .param("email", "ghost@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Gagal demote")));
    }

    @Test
    void testBanUserFailure() throws Exception {
        Mockito.when(authService.banUser("ghost@example.com")).thenReturn(null);

        mockMvc.perform(post("/api/v1/auth/admin/ban")
                        .param("email", "ghost@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Gagal melakukan banned")));
    }

    // --- RATING TESTS ---

    @Test
    void testAddRatingSuccess() throws Exception {
        User user = new User();
        Mockito.when(authService.addRating("jastiper@example.com", 5)).thenReturn(user);

        mockMvc.perform(post("/api/v1/auth/rating")
                        .param("jastiperEmail", "jastiper@example.com")
                        .param("ratingScore", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void testAddRatingFailure() throws Exception {
        Mockito.when(authService.addRating("ghost@example.com", 6))
                .thenThrow(new IllegalArgumentException("Rating harus antara 1 dan 5"));

        mockMvc.perform(post("/api/v1/auth/rating")
                        .param("jastiperEmail", "ghost@example.com")
                        .param("ratingScore", "6"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Rating harus antara 1 dan 5"));
    }
}