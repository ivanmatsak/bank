package com.example.bankcards.controller;

import com.example.bankcards.dto.BalanceRequest;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Управление картами", description = "Операции с банковскими картами для пользователей и администраторов")
@SecurityRequirement(name = "Bearer Authentication")
public class UserCardController {
    private final CardService cardService;

    @Operation(summary = "Экстренная блокировка своей карты", description = "Пользователь может только заблокировать карту, но не разблокировать")
    @PostMapping("/block")
    public ResponseEntity<Card> blockCard(
            @Valid @RequestBody CardRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(cardService.blockOwnCard(request.getCardNumber(), currentUser.getId()));
    }

    @Operation(summary = "Просмотр списка собственных карт", description = "Возвращает только те карты, которые принадлежат текущему пользователю")
    @GetMapping
    public ResponseEntity<Page<Card>> getAllMyCards(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CardStatus status) {
        return ResponseEntity.ok(cardService.getMyCards(currentUser.getId(), page, size, status));
    }

    @Operation(summary = "Получение данных карты по ID", description = "Доступно только владельцу карты")
    @GetMapping("/{id}")
    public ResponseEntity<Card> getById(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(cardService.getCardByIdForUser(id, currentUser.getId()));
    }

    @Operation(summary = "Просмотр баланса по номеру карты", description = "Номер карты передается в теле запроса для безопасности")
    @PostMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @Valid @RequestBody BalanceRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(cardService.getCardBalance(request.getCardNumber(), currentUser.getId()));
    }
}
