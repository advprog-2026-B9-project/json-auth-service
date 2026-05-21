package com.b9.json.jsonplatform.auth.infrastructure.controller;

import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.application.service.KycService;
import com.b9.json.jsonplatform.auth.domain.KycStatus;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.domain.UserRole;
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

    // --- REGISTER & LOGIN TESTS ---

    @Test
    void testRegisterUser() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setUsername("customUser");

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
        user.setEmail("test@example.com");

        Mockito.when(authService.loginUser("test@example.com", "password123")).thenReturn(user);

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

    // --- KYC TESTS ---

    @Test
    void testSubmitKycSuccess() throws Exception {
        User user = new User();
        user.setKycStatus(KycStatus.PENDING_VERIFICATION);
        Mockito.when(kycService.submitKyc(anyString(), anyString(), anyString(), anyString())).thenReturn(user);

        KycRequest request = new KycRequest();
        request.setEmail("test@example.com");
        request.setNikKtp("1234567890123456");

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

    // --- ADMIN TESTS ---

    @Test
    void testGetPendingKycForbidden() throws Exception {
        User notAdmin = new User();
        notAdmin.setRole(UserRole.TITIPERS);
        Mockito.when(authService.findByEmail("user@example.com")).thenReturn(notAdmin);

        mockMvc.perform(get("/api/v1/auth/admin/kyc/pending")
                        .param("requesterEmail", "user@example.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testReviewKycSuccess() throws Exception {
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        Mockito.when(authService.findByEmail("admin@example.com")).thenReturn(admin);
        Mockito.when(kycService.reviewKyc("user@example.com", true)).thenReturn(new User());

        KycReviewRequest request = new KycReviewRequest();
        request.setEmail("user@example.com");
        request.setApproved(true);

        mockMvc.perform(post("/api/v1/auth/admin/kyc/review")
                        .param("requesterEmail", "admin@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("KYC Disetujui")));
    }

    @Test
    void testDemoteUserSuccess() throws Exception {
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        Mockito.when(authService.findByEmail("admin@example.com")).thenReturn(admin);
        Mockito.when(authService.demoteJastiper("jastiper@example.com")).thenReturn(new User());

        mockMvc.perform(post("/api/v1/auth/admin/demote")
                        .param("requesterEmail", "admin@example.com")
                        .param("email", "jastiper@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    void testBanUserSuccess() throws Exception {
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        Mockito.when(authService.findByEmail("admin@example.com")).thenReturn(admin);
        Mockito.when(authService.banUser("user@example.com")).thenReturn(new User());

        mockMvc.perform(post("/api/v1/auth/admin/ban")
                        .param("requesterEmail", "admin@example.com")
                        .param("email", "user@example.com"))
                .andExpect(status().isOk());
    }

    // --- OTHER TESTS ---

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
    void testGetUserByUsernameSuccess() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setFullName("Test User");
        Mockito.when(authService.findByUsername("testuser")).thenReturn(user);

        mockMvc.perform(get("/api/v1/auth/internal/user")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}