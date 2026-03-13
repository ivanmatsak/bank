package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("POST /internal - успешный перевод")
    void internalTransfer_Success() throws Exception {
        TransferRequest request = new TransferRequest(
                "1111222233334444",
                "5555666677778888",
                "500.00"
        );

        doNothing().when(transferService).transferBetweenOwnCards(
                eq(1L), anyString(), anyString(), any(BigDecimal.class));

        mockMvc.perform(post("/api/v1/transfers/internal")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Перевод успешно выполнен"));
    }

    @Test
    @DisplayName("POST /internal - ошибка валидации (отрицательная сумма)")
    void internalTransfer_ValidationError() throws Exception {
        TransferRequest invalidRequest = new TransferRequest(
                "1111222233334444",
                "5555666677778888",
                "-100.00"
        );

        mockMvc.perform(post("/api/v1/transfers/internal")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /internal - ошибка: недостаточно средств (бизнес-логика)")
    void internalTransfer_ServiceError() throws Exception {
        TransferRequest request = new TransferRequest(
                "1111222233334444", "5555666677778888", "10000.00");

        doThrow(new RuntimeException("Недостаточно средств"))
                .when(transferService).transferBetweenOwnCards(anyLong(), anyString(), anyString(), any());

        mockMvc.perform(post("/api/v1/transfers/internal")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Недостаточно средств"));
    }
}