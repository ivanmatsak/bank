package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос баланса по конкретной карте")
public class BalanceRequest {

    @Schema(description = "Полный номер карты (16 цифр)", example = "1234567812345678")
    @NotBlank(message = "Номер карты не может быть пустым")
    @Pattern(regexp = "^\\d{16}$", message = "Номер карты должен состоять ровно из 16 цифр")
    private String cardNumber;
}