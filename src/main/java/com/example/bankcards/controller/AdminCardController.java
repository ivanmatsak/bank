package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardStatusRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards/admin")
@RequiredArgsConstructor
@Tag(name = "Управление картами", description = "Операции с банковскими картами для пользователей и администраторов")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminCardController {
    private final CardService cardService;

    @Operation(summary = "Просмотр всех карт в системе (ADMIN)", description = "Доступно только администратору")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Card>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) CardStatus status) {
        return ResponseEntity.ok(cardService.getAllCards(page, size, status));
    }

    @Operation(summary = "Изменение статуса любой карты (ADMIN)", description = "Активация, блокировка или установка статуса 'Истек'")
    @PatchMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Card> changeStatus(@Valid @RequestBody CardStatusRequest request) {
        return ResponseEntity.ok(cardService.updateCardStatusByNumber(request.getCardNumber(), request.getStatus()));
    }

    @Operation(summary = "Создание карты для любого пользователя (ADMIN)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Card> create(@Valid @RequestBody CardRequest dto) {
        return ResponseEntity.ok(cardService.createCardByAdmin(dto));
    }

    @Operation(summary = "Удаление карты по номеру (ADMIN)")
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@Valid @RequestBody CardRequest request) {
        cardService.deleteCardByNumber(request.getCardNumber());
        return ResponseEntity.noContent().build();
    }
}
