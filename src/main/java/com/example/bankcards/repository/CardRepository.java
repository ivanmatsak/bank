package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findAllByUserId(Long userId, Pageable pageable);

    Page<Card> findAllByUserIdAndStatus(Long userId, CardStatus status, Pageable pageable);

    Page<Card> findAll(Pageable pageable);

    Page<Card> findAllByStatus(CardStatus status, Pageable pageable);

    Optional<Card> findByCardNumber(String cardNumber);

}