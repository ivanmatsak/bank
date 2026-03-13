package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public Page<Card> getAllCards(int page, int size, CardStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (status != null) {
            return cardRepository.findAllByStatus(status, pageable);
        }
        return cardRepository.findAll(pageable);
    }

    @Transactional
    public Card updateCardStatusByNumber(String cardNumber, CardStatus newStatus) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new EntityNotFoundException("Карта с номером " + cardNumber + " не найдена"));

        card.setStatus(newStatus);
        return cardRepository.save(card);
    }

    @Transactional
    public Card blockOwnCard(String cardNumber, Long userId) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new EntityNotFoundException("Карта с номером " + cardNumber + " не найдена"));

        if (!card.getUser().getId().equals(userId)) {
            throw new RuntimeException("Доступ запрещен: вы не являетесь владельцем этой карты");
        }

        card.setStatus(CardStatus.BANNED);
        return cardRepository.save(card);
    }

    public Page<Card> getMyCards(Long userId, int page, int size, CardStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (status != null) {
            return cardRepository.findAllByUserIdAndStatus(userId, status, pageable);
        }
        return cardRepository.findAllByUserId(userId, pageable);
    }

    public Card getCardByIdForUser(Long id, Long userId) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Карта с ID " + id + " не найдена"));

        if (!card.getUser().getId().equals(userId)) {
            throw new RuntimeException("Доступ к данной карте запрещен");
        }
        return card;
    }

    @Transactional
    public Card createCardByAdmin(CardRequest dto) {
        if (cardRepository.findByCardNumber(dto.getCardNumber()).isPresent()) {
            throw new DataIntegrityViolationException("Карта с номером " + dto.getCardNumber() + " уже существует");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + dto.getUserId() + " не найден"));

        Card card = new Card();
        card.setCardNumber(dto.getCardNumber());
        card.setExpirationDate(dto.getExpirationDate());
        card.setStatus(dto.getStatus());
        card.setBalance(dto.getBalance());
        card.setOwner(dto.getOwner());
        card.setUser(user);

        return cardRepository.save(card);
    }

    @Transactional
    public void deleteCardByNumber(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new EntityNotFoundException("Карта с номером " + cardNumber + " не найдена"));

        cardRepository.delete(card);
    }

    public BigDecimal getCardBalance(String cardNumber, Long userId) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new EntityNotFoundException("Карта с номером " + cardNumber + " не найдена"));

        if (!card.getUser().getId().equals(userId)) {
            throw new RuntimeException("Доступ к балансу данной карты запрещен");
        }
        return card.getBalance();
    }
}