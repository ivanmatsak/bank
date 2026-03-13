package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final CardRepository cardRepository;

    @Transactional
    public void transferBetweenOwnCards(Long userId, String fromNumber, String toNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Сумма должна быть больше нуля");
        }

        Card fromCard = cardRepository.findByCardNumber(fromNumber)
                .orElseThrow(() -> new RuntimeException("Карта списания не найдена"));
        Card toCard = cardRepository.findByCardNumber(toNumber)
                .orElseThrow(() -> new RuntimeException("Карта зачисления не найдена"));

        if (!fromCard.getUser().getId().equals(userId) || !toCard.getUser().getId().equals(userId)) {
            throw new RuntimeException("Перевод возможен только между вашими собственными картами");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("Одна из карт заблокирована или неактивна");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Недостаточно средств на карте списания");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}
