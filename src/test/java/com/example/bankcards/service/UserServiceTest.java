package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("Получение всех пользователей: проверка пагинации")
    void getAllUsers_Success() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        Page<User> result = userService.getAllUsers(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Обновление роли: успешная смена USER на ADMIN")
    void updateUserRole_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User updatedUser = userService.updateUserRole(1L, Role.ADMIN);

        assertEquals(Role.ADMIN, updatedUser.getRole());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Удаление: ошибка при попытке удалить самого себя")
    void deleteUser_SelfDeletion_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(1L, 1L));

        assertEquals("Вы не можете удалить свой собственный аккаунт", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Удаление: успешное удаление другого пользователя")
    void deleteUser_Success() {
        Long userIdToDelete = 2L;
        Long adminId = 1L;
        User userToDelete = User.builder().id(userIdToDelete).username("other").build();

        when(userRepository.findById(userIdToDelete)).thenReturn(Optional.of(userToDelete));

        userService.deleteUser(userIdToDelete, adminId);

        verify(userRepository).delete(userToDelete);
    }

    @Test
    @DisplayName("Обновление роли: ошибка, если пользователь не найден")
    void updateUserRole_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUserRole(99L, Role.ADMIN));
    }
}