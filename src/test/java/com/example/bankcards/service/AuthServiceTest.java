package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .password("raw_password")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("Регистрация: сохранение пользователя и генерация токена")
    void register_Success() {
        when(passwordEncoder.encode("raw_password")).thenReturn("encoded_password");
        when(jwtService.generateToken(any(User.class))).thenReturn("test_token");

        String token = authService.register(testUser);

        assertEquals("test_token", token);
        assertEquals("encoded_password", testUser.getPassword());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Логин: успешная аутентификация и возврат токена")
    void login_Success() {
        String username = "testuser";
        String password = "raw_password";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("test_token");

        String token = authService.login(username, password);

        assertEquals("test_token", token);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Логин: ошибка, если пользователь не найден")
    void login_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login("unknown", "password"));
    }
}