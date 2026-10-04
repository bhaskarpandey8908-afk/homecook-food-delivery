package com.homecook.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.homecook.entity.User;
import com.homecook.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Register user
    public User register(User user) {

        if (user == null) {
            throw new IllegalArgumentException("User data is required");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (userRepository
                .findByEmail(user.getEmail())
                .isPresent()) {

            throw new RuntimeException(
                    "Email already registered");
        }

        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );

        if (user.getRole() == null ||
                user.getRole().isBlank()) {

            user.setRole("CUSTOMER");
        }

        return userRepository.save(user);
    }

    // Login
    public User login(
            String email,
            String password) {

        if (email == null || password == null) {
            return null;
        }

        User user = userRepository
                .findByEmail(email.trim())
                .orElse(null);

        if (user == null || user.isBlocked()) {
            return null;
        }

        if (passwordEncoder.matches(
                password,
                user.getPassword())) {

            return user;
        }

        return null;
    }

    public User loginByRole(
            String email,
            String password,
            String role) {

        User user = login(email, password);

        if (user == null) {
            return null;
        }

        if (role == null ||
                role.isBlank()) {
            return user;
        }

        return role.equalsIgnoreCase(user.getRole()) ? user : null;
    }

    // Find user
    public User findByEmail(String email) {

        return userRepository
                .findByEmail(email)
                .orElse(null);
    }

    // Find user by ID
    public User findById(Long id) {

        return userRepository
                .findById(id)
                .orElse(null);
    }

    // Save user
    public User saveUser(User user) {

        return userRepository.save(user);
    }

    public User toggleBlockStatus(Long id) {

        User user = findById(id);
        if (user == null) {
            return null;
        }

        user.setBlocked(!user.isBlocked());
        return userRepository.save(user);
    }

    // Delete user
    public void deleteUser(Long id) {

        userRepository.deleteById(id);
    }
}