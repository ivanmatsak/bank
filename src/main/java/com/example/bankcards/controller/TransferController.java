package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Денежные переводы", description = "Операции по перемещению средств между картами")
@SecurityRequirement(name = "Bearer Authentication")
public class TransferController {
    private final TransferService transferService;

    @Operation(
            summary = "Внутренний перевод между своими картами",
            description = "Позволяет перевести средства, если обе карты принадлежат текущему пользователю и активны"
    )
    @PostMapping("/internal")
    public ResponseEntity<String> internalTransfer(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TransferRequest request) {

        BigDecimal amount = new BigDecimal(request.getAmount());

        transferService.transferBetweenOwnCards(
                currentUser.getId(),
                request.getFromCardNumber(),
                request.getToCardNumber(),
                amount
        );

        return ResponseEntity.ok("Перевод успешно выполнен");
    }
}