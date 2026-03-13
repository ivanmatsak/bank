package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на изменение статуса карты администратором")
public class CardStatusRequest {

    @Schema(description = "Полный номер карты (16 цифр)", example = "1234567812345678")
    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = "^\\d{16}$", message = "Номер карты должен состоять из 16 цифр")
    private String cardNumber;

    @Schema(description = "Новый статус карты", example = "BANNED")
    @NotNull(message = "Статус обязателен")
    private CardStatus status;
}