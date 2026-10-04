package com.homecook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.homecook.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    java.util.List<User> findByRole(String role);

    java.util.List<User> findByRoleOrderByIdDesc(String role);

}