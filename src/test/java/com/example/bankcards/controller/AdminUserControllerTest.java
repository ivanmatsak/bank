package com.example.bankcards.controller;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc
@EnableMethodSecurity
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .role(Role.ADMIN)
                .build();

        targetUser = User.builder()
                .id(2L)
                .username("user")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/admin/users - получение списка пользователей")
    void getAllUsers_Success() throws Exception {
        Page<User> page = new PageImpl<>(List.of(targetUser, adminUser));
        when(userService.getAllUsers(anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].username").value("user"));
    }

    @Test
    @DisplayName("PATCH /role - смена роли пользователя")
    void changeRole_Success() throws Exception {
        targetUser.setRole(Role.ADMIN);
        when(userService.updateUserRole(eq(2L), eq(Role.ADMIN))).thenReturn(targetUser);

        mockMvc.perform(patch("/api/v1/admin/users/2/role")
                        .with(user(adminUser))
                        .with(csrf())
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("DELETE /{id} - удаление пользователя")
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/2")
                        .with(user(adminUser))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(2L, 1L);
    }

    @Test
    @DisplayName("DELETE /{id} - ошибка при удалении самого себя")
    void deleteSelf_Forbidden() throws Exception {
        doThrow(new RuntimeException("Вы не можете удалить свой собственный аккаунт"))
                .when(userService).deleteUser(1L, 1L);

        mockMvc.perform(delete("/api/v1/admin/users/1")
                        .with(user(adminUser))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Вы не можете удалить свой собственный аккаунт"));
    }
}