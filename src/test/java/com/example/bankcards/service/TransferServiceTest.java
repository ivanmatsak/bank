package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransferService transferService;

    private User user;
    private Card cardFrom;
    private Card cardTo;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();

        cardFrom = new Card();
        cardFrom.setId(10L);
        cardFrom.setCardNumber("1111222233334444");
        cardFrom.setBalance(new BigDecimal("1000.00"));
        cardFrom.setStatus(CardStatus.ACTIVE);
        cardFrom.setUser(user);

        cardTo = new Card();
        cardTo.setId(11L);
        cardTo.setCardNumber("5555666677778888");
        cardTo.setBalance(new BigDecimal("100.00"));
        cardTo.setStatus(CardStatus.ACTIVE);
        cardTo.setUser(user);
    }

    @Test
    @DisplayName("Успешный перевод между своими картами")
    void transfer_Success() {
        BigDecimal amount = new BigDecimal("500.00");
        when(cardRepository.findByCardNumber(cardFrom.getCardNumber())).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findByCardNumber(cardTo.getCardNumber())).thenReturn(Optional.of(cardTo));

        transferService.transferBetweenOwnCards(user.getId(), cardFrom.getCardNumber(), cardTo.getCardNumber(), amount);

        assertEquals(new BigDecimal("500.00"), cardFrom.getBalance());
        assertEquals(new BigDecimal("600.00"), cardTo.getBalance());
        verify(cardRepository).save(cardFrom);
        verify(cardRepository).save(cardTo);
    }

    @Test
    @DisplayName("Ошибка: недостаточно средств")
    void transfer_InsufficientFunds_ThrowsException() {
        BigDecimal amount = new BigDecimal("2000.00");
        when(cardRepository.findByCardNumber(cardFrom.getCardNumber())).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findByCardNumber(cardTo.getCardNumber())).thenReturn(Optional.of(cardTo));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transferService.transferBetweenOwnCards(user.getId(), cardFrom.getCardNumber(), cardTo.getCardNumber(), amount));

        assertTrue(ex.getMessage().contains("Недостаточно средств"));
    }

    @Test
    @DisplayName("Ошибка: попытка перевода с чужой карты")
    void transfer_AlienCard_ThrowsException() {
        Long currentUserId = 1L;

        User anotherUser = User.builder().id(99L).build();
        cardFrom.setUser(anotherUser);

        cardTo.setUser(User.builder().id(currentUserId).build());

        when(cardRepository.findByCardNumber(cardFrom.getCardNumber())).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findByCardNumber(cardTo.getCardNumber())).thenReturn(Optional.of(cardTo));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transferService.transferBetweenOwnCards(currentUserId, cardFrom.getCardNumber(), cardTo.getCardNumber(), new BigDecimal("10.00")));

        assertTrue(ex.getMessage().contains("вашими собственными картами"));
    }

    @Test
    @DisplayName("Ошибка: одна из карт заблокирована")
    void transfer_InactiveCard_ThrowsException() {
        cardTo.setStatus(CardStatus.BANNED);
        when(cardRepository.findByCardNumber(cardFrom.getCardNumber())).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findByCardNumber(cardTo.getCardNumber())).thenReturn(Optional.of(cardTo));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transferService.transferBetweenOwnCards(user.getId(), cardFrom.getCardNumber(), cardTo.getCardNumber(), new BigDecimal("10.00")));

        assertTrue(ex.getMessage().contains("заблокирована или неактивна"));
    }
}