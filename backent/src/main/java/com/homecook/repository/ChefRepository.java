package com.homecook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.homecook.entity.Chef;

public interface ChefRepository extends JpaRepository<Chef, Long> {

    List<Chef> findByApprovedTrue();
}