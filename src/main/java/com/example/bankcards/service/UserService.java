package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Page<User> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    @Transactional
    public User updateUserRole(Long userId, Role newRole) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setRole(newRole);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    @Transactional
    public void deleteUser(Long userId, Long currentAdminId) {
        if (userId.equals(currentAdminId)) {
            throw new RuntimeException("Вы не можете удалить свой собственный аккаунт");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));

        userRepository.delete(user);
    }
}