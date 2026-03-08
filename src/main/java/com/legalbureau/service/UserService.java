package com.legalbureau.service;

import com.legalbureau.entity.enums.Role;
import lombok.RequiredArgsConstructor;
import com.legalbureau.entity.User;
import com.legalbureau.exception.DuplicateResourceException;
import com.legalbureau.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public java.util.Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void registerClient(User user) {
        validatePhoneNumber(user.getPhone());
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Користувач з email " + user.getEmail() + " вже існує");
        }
        user.setRole(Role.CLIENT);
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        userRepository.save(user);
    }

    public List<User> findAllClients() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.CLIENT)
                .collect(Collectors.toList());
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Користувача не знайдено"));
    }

    public Page<User> getFilteredClients(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String safeSearch = (search == null || search.trim().isEmpty()) ? null : "%" + search.trim().toLowerCase() + "%";
        return userRepository.findFilteredClients(safeSearch, pageable);
    }

    public void createClientByAdmin(User client) {
        validatePhoneNumber(client.getPhone());
        if (userRepository.existsByEmail(client.getEmail())) {
            throw new DuplicateResourceException("Email вже зайнятий!");
        }
        client.setRole(Role.CLIENT);
        client.setPasswordHash(passwordEncoder.encode(client.getPasswordHash()));
        userRepository.save(client);
    }

    public void updateClientByAdmin(Long id, User clientData) {
        validatePhoneNumber(clientData.getPhone());
        User existing = findById(id);

        if (!existing.getEmail().equals(clientData.getEmail()) && userRepository.existsByEmail(clientData.getEmail())) {
            throw new DuplicateResourceException("Цей Email вже використовується іншим акаунтом!");
        }

        existing.setFullName(clientData.getFullName());
        existing.setPhone(clientData.getPhone());
        existing.setEmail(clientData.getEmail());

        if (clientData.getPasswordHash() != null && !clientData.getPasswordHash().isEmpty()) {
            existing.setPasswordHash(passwordEncoder.encode(clientData.getPasswordHash()));
        }

        userRepository.save(existing);
    }

    public User toggleUserStatus(Long id) {
        User user = findById(id);
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Користувача не знайдено");
        }
        userRepository.deleteById(id);
    }

    private void validatePhoneNumber(String phone) {
        if (phone == null || !phone.matches("^\\+380-\\d{3}-\\d{3}-\\d{3}$")) {
            throw new IllegalArgumentException("Невірний формат телефону! Використовуйте: +380-XXX-XXX-XXX");
        }
    }
}