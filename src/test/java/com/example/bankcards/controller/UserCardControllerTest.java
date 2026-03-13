package com.example.bankcards.controller;

import com.example.bankcards.dto.BalanceRequest;
import com.example.bankcards.dto.CardRequest;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserCardController.class)
@AutoConfigureMockMvc
class UserCardControllerTest {

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .role(Role.USER)
                .build();

        testCard = new Card();
        testCard.setId(100L);
        testCard.setCardNumber("1111222233334444");
        testCard.setBalance(BigDecimal.valueOf(1000));
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setUser(testUser);
    }

    @Test
    @DisplayName("GET /api/v1/cards - получение своих карт")
    void getAllMyCards_Success() throws Exception {
        Page<Card> page = new PageImpl<>(List.of(testCard));

        when(cardService.getMyCards(anyLong(), anyInt(), anyInt(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/cards")
                        .with(user(testUser))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/cards/balance - запрос баланса")
    void getBalance_Success() throws Exception {
        BalanceRequest request = new BalanceRequest("1111222233334444");

        when(cardService.getCardBalance(eq("1111222233334444"), eq(1L)))
                .thenReturn(BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/v1/cards/balance")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("1000"));
    }

    @Test
    @DisplayName("POST /api/v1/cards/block - блокировка карты")
    void blockCard_Success() throws Exception {
        CardRequest request = new CardRequest();
        request.setCardNumber("1111222233334444");
        request.setOwner("Test Owner");
        request.setExpirationDate(LocalDate.now().plusYears(1));
        request.setStatus(CardStatus.ACTIVE);
        request.setBalance(BigDecimal.TEN);
        request.setUserId(1L);

        testCard.setStatus(CardStatus.BANNED);

        when(cardService.blockOwnCard(eq("1111222233334444"), eq(1L))).thenReturn(testCard);

        mockMvc.perform(post("/api/v1/cards/block")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BANNED"));
    }

    @Test
    @DisplayName("GET /api/v1/cards/{id} - получение карты по ID")
    void getById_Success() throws Exception {
        when(cardService.getCardByIdForUser(eq(100L), anyLong())).thenReturn(testCard);

        mockMvc.perform(get("/api/v1/cards/100")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("POST /api/v1/cards/balance - ошибка валидации (короткий номер)")
    void getBalance_ValidationError() throws Exception {
        BalanceRequest invalidRequest = new BalanceRequest("123");

        mockMvc.perform(post("/api/v1/cards/balance")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}