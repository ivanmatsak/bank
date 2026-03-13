package com.example.bankcards.controller;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Администрирование пользователей", description = "Управление учетными записями (только для ADMIN)")
@SecurityRequirement(name = "Bearer Authentication")
@Validated
public class AdminUserController {
    private final UserService userService;

    @Operation(summary = "Получить список всех пользователей", description = "Возвращает страницу со всеми пользователями системы")
    @GetMapping
    public ResponseEntity<Page<User>> getAll(
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество записей на странице") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @Operation(summary = "Изменить роль пользователя", description = "Позволяет повысить пользователя до ADMIN или понизить до USER")
    @PatchMapping("/{id}/role")
    public ResponseEntity<User> changeRole(
            @Parameter(description = "ID пользователя") @PathVariable Long id,
            @Parameter(description = "Новая роль") @RequestParam Role role) {
        return ResponseEntity.ok(userService.updateUserRole(id, role));
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя и все его карты. Нельзя удалить самого себя.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID удаляемого пользователя") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {

        userService.deleteUser(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}