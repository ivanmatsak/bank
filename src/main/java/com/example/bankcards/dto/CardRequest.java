package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Данные для создания новой карты администратором")
public class CardRequest {

    @Schema(description = "Полный номер карты (16 цифр)", example = "4444555566667777")
    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = "^\\d{16}$", message = "Номер карты должен состоять из 16 цифр")
    private String cardNumber;

    @Schema(description = "Срок действия карты", example = "2030-12-31")
    @NotNull(message = "Срок действия обязателен")
    @Future(message = "Срок действия должен быть в будущем")
    private LocalDate expirationDate;

    @Schema(description = "Начальный статус карты", example = "ACTIVE")
    @NotNull(message = "Статус обязателен")
    private CardStatus status;

    @Schema(description = "Начальный баланс", example = "1000.00")
    @NotNull(message = "Баланс обязателен")
    @DecimalMin(value = "0.0", message = "Баланс не может быть отрицательным")
    private BigDecimal balance;

    @Schema(description = "Имя владельца (напр. на латинице)", example = "IVAN IVANOV")
    @NotBlank(message = "Имя владельца обязательно")
    private String owner;

    @Schema(description = "ID пользователя, которому принадлежит карта", example = "1")
    @NotNull(message = "ID пользователя обязателен")
    private Long userId;
}