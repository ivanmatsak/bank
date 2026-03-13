package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardStatusRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AdminCardController.class)
@AutoConfigureMockMvc
@EnableMethodSecurity
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private Card testCard;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .role(Role.ADMIN)
                .build();

        testCard = new Card();
        testCard.setId(100L);
        testCard.setCardNumber("1111222233334444");
        testCard.setStatus(CardStatus.ACTIVE);
    }

    @Test
    @DisplayName("GET /all - получение всех карт админом")
    void getAllCards_Success() throws Exception {
        Page<Card> page = new PageImpl<>(List.of(testCard));
        when(cardService.getAllCards(anyInt(), anyInt(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/cards/admin/all")
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** 4444"))
                .andExpect(jsonPath("$.content[0].id").value(100));
    }


    @Test
    @DisplayName("POST / - создание карты админом")
    void createCard_Success() throws Exception {
        CardRequest request = new CardRequest();
        request.setCardNumber("5555666677778888");
        request.setUserId(2L);
        request.setBalance(BigDecimal.valueOf(1000));
        request.setOwner("IVAN IVANOV");
        request.setExpirationDate(LocalDate.now().plusYears(1));
        request.setStatus(CardStatus.ACTIVE);

        when(cardService.createCardByAdmin(any(CardRequest.class))).thenReturn(testCard);

        mockMvc.perform(post("/api/v1/cards/admin")
                        .with(user(adminUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /status - смена статуса админом")
    void changeStatus_Success() throws Exception {
        CardStatusRequest request = new CardStatusRequest("1111222233334444", CardStatus.BANNED);

        testCard.setStatus(CardStatus.BANNED);
        when(cardService.updateCardStatusByNumber(anyString(), any())).thenReturn(testCard);

        mockMvc.perform(patch("/api/v1/cards/admin/status")
                        .with(user(adminUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BANNED"));
    }

    @Test
    @DisplayName("DELETE / - удаление карты по номеру")
    void deleteCard_Success() throws Exception {
        CardRequest request = new CardRequest();
        request.setCardNumber("1111222233334444");
        request.setOwner("ADMIN");
        request.setExpirationDate(LocalDate.now().plusYears(1));
        request.setStatus(CardStatus.ACTIVE);
        request.setBalance(BigDecimal.ZERO);
        request.setUserId(1L);

        mockMvc.perform(delete("/api/v1/cards/admin")
                        .with(user(adminUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Ошибка 400 при попытке доступа USER к админ-методам")
    void adminMethod_AccessDenied_ForUser() throws Exception {
        User regularUser = User.builder().username("user").role(Role.USER).build();

        mockMvc.perform(get("/api/v1/cards/admin/all")
                        .with(user(regularUser)))
                .andExpect(status().isBadRequest());
    }
}