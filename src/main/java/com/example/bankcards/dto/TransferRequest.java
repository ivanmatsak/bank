package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на перевод средств между собственными картами")
public class TransferRequest {

    @Schema(description = "Номер карты списания (16 цифр)", example = "1111222233334444")
    @NotBlank(message = "Номер карты списания обязателен")
    @Pattern(regexp = "^\\d{16}$", message = "Номер карты списания должен состоять из 16 цифр")
    private String fromCardNumber;

    @Schema(description = "Номер карты зачисления (16 цифр)", example = "5555666677778888")
    @NotBlank(message = "Номер карты зачисления обязателен")
    @Pattern(regexp = "^\\d{16}$", message = "Номер карты зачисления должен состоять из 16 цифр")
    private String toCardNumber;

    @Schema(description = "Сумма перевода", example = "500.00")
    @NotBlank(message = "Сумма перевода обязательна")
    @DecimalMin(value = "0.01", message = "Минимальная сумма перевода — 0.01")
    private String amount;
}