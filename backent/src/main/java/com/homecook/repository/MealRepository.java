package com.homecook.repository;

import com.homecook.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {

    List<Meal> findByAvailableTrue();

    List<Meal> findByChefId(Long chefId);

}
