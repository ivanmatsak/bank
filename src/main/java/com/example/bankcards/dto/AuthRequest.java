package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на аутентификацию или регистрацию")
public class AuthRequest {

    @Schema(description = "Имя пользователя (логин)", example = "admin")
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    private String username;

    @Schema(description = "Пароль", example = "admin123")
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 4, message = "Пароль должен содержать минимум 4 символа")
    private String password;
}