package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CardService cardService;

    private User owner;
    private Card testCard;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).username("owner").build();
        testCard = new Card();
        testCard.setId(100L);
        testCard.setCardNumber("1111222233334444");
        testCard.setUser(owner);
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setBalance(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Админ: получение всех карт БЕЗ фильтра по статусу")
    void getAllCards_NoStatus_ReturnsAll() {
        int page = 0;
        int size = 10;
        Page<Card> expectedPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Card> result = cardService.getAllCards(page, size, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(cardRepository).findAll(any(Pageable.class));
        verify(cardRepository, never()).findAllByStatus(any(), any());
    }

    @Test
    @DisplayName("Админ: получение всех карт С фильтром по статусу")
    void getAllCards_WithStatus_ReturnsFiltered() {
        int page = 0;
        int size = 10;
        CardStatus status = CardStatus.BANNED;
        testCard.setStatus(status);
        Page<Card> expectedPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.findAllByStatus(eq(status), any(Pageable.class))).thenReturn(expectedPage);

        Page<Card> result = cardService.getAllCards(page, size, status);

        assertNotNull(result);
        assertEquals(status, result.getContent().get(0).getStatus());

        verify(cardRepository).findAllByStatus(eq(status), any(Pageable.class));
        verify(cardRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Админ: смена статуса по номеру карты. Успех")
    void updateCardStatusByNumber_Success() {
        String cardNumber = "1111222233334444";
        CardStatus newStatus = CardStatus.BANNED;

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArguments()[0]);

        Card result = cardService.updateCardStatusByNumber(cardNumber, newStatus);

        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        verify(cardRepository).findByCardNumber(cardNumber);
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Админ: смена статуса. Ошибка - карта не найдена")
    void updateCardStatusByNumber_NotFound_ThrowsException() {
        String unknownNumber = "0000000000000000";
        when(cardRepository.findByCardNumber(unknownNumber)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> cardService.updateCardStatusByNumber(unknownNumber, CardStatus.ACTIVE));

        assertTrue(exception.getMessage().contains("не найдена"));
        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Пользователь: успешная блокировка собственной карты")
    void blockOwnCard_Success() {
        String cardNumber = testCard.getCardNumber();
        Long userId = owner.getId();

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        Card result = cardService.blockOwnCard(cardNumber, userId);

        assertEquals(CardStatus.BANNED, result.getStatus());
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Пользователь: ошибка при попытке заблокировать чужую карту")
    void blockOwnCard_Forbidden_ThrowsException() {
        String cardNumber = testCard.getCardNumber();
        Long strangerId = 999L;

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cardService.blockOwnCard(cardNumber, strangerId));

        assertEquals("Доступ запрещен: вы не являетесь владельцем этой карты", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Пользователь: блокировка. Ошибка - карта не найдена")
    void blockOwnCard_NotFound_ThrowsException() {
        String unknownNumber = "0000";
        when(cardRepository.findByCardNumber(unknownNumber)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> cardService.blockOwnCard(unknownNumber, 1L));
    }

    @Test
    @DisplayName("Пользователь: получение своих карт БЕЗ фильтра по статусу")
    void getMyCards_NoStatus_ReturnsAllUserCards() {
        Long userId = owner.getId();
        Page<Card> expectedPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.findAllByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Card> result = cardService.getMyCards(userId, 0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(cardRepository).findAllByUserId(eq(userId), any(Pageable.class));
        verify(cardRepository, never()).findAllByUserIdAndStatus(anyLong(), any(), any());
    }

    @Test
    @DisplayName("Пользователь: получение своих карт С фильтром по статусу")
    void getMyCards_WithStatus_ReturnsFilteredUserCards() {
        Long userId = owner.getId();
        CardStatus status = CardStatus.ACTIVE;
        Page<Card> expectedPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.findAllByUserIdAndStatus(eq(userId), eq(status), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Card> result = cardService.getMyCards(userId, 0, 10, status);

        assertNotNull(result);
        assertEquals(status, result.getContent().get(0).getStatus());

        verify(cardRepository).findAllByUserIdAndStatus(eq(userId), eq(status), any(Pageable.class));
        verify(cardRepository, never()).findAllByUserId(anyLong(), any());
    }

    @Test
    @DisplayName("Админ: создание карты. Успех")
    void createCardByAdmin_Success() {
        CardRequest dto = new CardRequest();
        dto.setCardNumber("5555666677778888");
        dto.setUserId(1L);
        dto.setBalance(BigDecimal.valueOf(500));
        dto.setExpirationDate(LocalDate.now().plusYears(1));
        dto.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findByCardNumber(dto.getCardNumber())).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        Card result = cardService.createCardByAdmin(dto);

        assertNotNull(result);
        assertEquals(dto.getCardNumber(), result.getCardNumber());
        assertEquals(owner, result.getUser());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Админ: создание. Ошибка - номер уже существует")
    void createCardByAdmin_DuplicateNumber_ThrowsException() {
        CardRequest dto = new CardRequest();
        dto.setCardNumber(testCard.getCardNumber());

        when(cardRepository.findByCardNumber(dto.getCardNumber())).thenReturn(Optional.of(testCard));

        assertThrows(DataIntegrityViolationException.class,
                () -> cardService.createCardByAdmin(dto));

        verify(userRepository, never()).findById(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Админ: создание. Ошибка - пользователь не найден")
    void createCardByAdmin_UserNotFound_ThrowsException() {
        CardRequest dto = new CardRequest();
        dto.setCardNumber("1111222233334444");
        dto.setUserId(99L);

        when(cardRepository.findByCardNumber(dto.getCardNumber())).thenReturn(Optional.empty());
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> cardService.createCardByAdmin(dto));

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Админ: удаление карты по номеру. Успех")
    void deleteCardByNumber_Success() {
        String cardNumber = testCard.getCardNumber();
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        cardService.deleteCardByNumber(cardNumber);

        verify(cardRepository).findByCardNumber(cardNumber);
        verify(cardRepository).delete(testCard);
    }

    @Test
    @DisplayName("Админ: удаление карты. Ошибка — карта не найдена")
    void deleteCardByNumber_NotFound_ThrowsException() {
        String unknownNumber = "0000111122223333";
        when(cardRepository.findByCardNumber(unknownNumber)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> cardService.deleteCardByNumber(unknownNumber));

        assertTrue(exception.getMessage().contains("не найдена"));

        verify(cardRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Баланс: успешное получение баланса собственной карты")
    void getCardBalance_Success() {
        String cardNumber = testCard.getCardNumber();
        Long userId = owner.getId();
        BigDecimal expectedBalance = testCard.getBalance();

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        BigDecimal result = cardService.getCardBalance(cardNumber, userId);

        assertEquals(expectedBalance, result);
        verify(cardRepository).findByCardNumber(cardNumber);
    }

    @Test
    @DisplayName("Баланс: ошибка при попытке посмотреть чужой баланс")
    void getCardBalance_Forbidden_ThrowsException() {
        String cardNumber = testCard.getCardNumber();
        Long strangerId = 999L;

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(testCard));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cardService.getCardBalance(cardNumber, strangerId));

        assertEquals("Доступ к балансу данной карты запрещен", exception.getMessage());
    }

    @Test
    @DisplayName("Баланс: ошибка — карта не найдена")
    void getCardBalance_NotFound_ThrowsException() {
        String unknownNumber = "0000";
        when(cardRepository.findByCardNumber(unknownNumber)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> cardService.getCardBalance(unknownNumber, 1L));
    }
}
