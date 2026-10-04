package com.homecook.service;

import com.homecook.entity.Chef;
import com.homecook.entity.Meal;
import com.homecook.repository.ChefRepository;
import com.homecook.repository.MealRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MealService {

    private final MealRepository mealRepository;
    private final ChefRepository chefRepository;

    public MealService(
            MealRepository mealRepository,
            ChefRepository chefRepository) {

        this.mealRepository = mealRepository;
        this.chefRepository = chefRepository;
    }

    // Add a new meal
    public Meal addMeal(Meal meal, Long chefId) {

        Chef chef = chefRepository
                .findById(chefId)
                .orElseThrow(() ->
                        new RuntimeException("Chef not found"));

        meal.setChef(chef);

        if (meal.getMenuDate() == null) {
            meal.setMenuDate(LocalDate.now());
        }

        meal.setAvailable(true);

        return mealRepository.save(meal);
    }

    // Save meal
    public Meal saveMeal(Meal meal) {

        return mealRepository.save(meal);
    }

    // Get all meals
    public List<Meal> getAllMeals() {

        return mealRepository.findAll();
    }

    // Get available meals
    public List<Meal> getAvailableMeals() {

        return mealRepository.findByAvailableTrue();
    }

    // Get meals belonging to a chef
    public List<Meal> getMealsByChef(Long chefId) {

        return mealRepository.findByChefId(chefId);
    }

    // Get meal by ID
    public Optional<Meal> getMealById(Long id) {

        return mealRepository.findById(id);
    }

    // Update meal
    public Meal updateMeal(Long id, Meal updatedMeal) {

        Meal meal = mealRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Meal not found"));

        meal.setName(updatedMeal.getName());
        meal.setDescription(updatedMeal.getDescription());
        meal.setPrice(updatedMeal.getPrice());
        meal.setCategory(updatedMeal.getCategory());
        meal.setMealTime(updatedMeal.getMealTime());
        meal.setFoodType(updatedMeal.getFoodType());
        meal.setImage(updatedMeal.getImage());
        meal.setQuantity(updatedMeal.getQuantity());
        meal.setAvailable(updatedMeal.isAvailable());

        return mealRepository.save(meal);
    }

    // Enable / disable meal
    public Meal updateAvailability(
            Long id,
            boolean available) {

        Meal meal = mealRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Meal not found"));

        meal.setAvailable(available);

        return mealRepository.save(meal);
    }

    // Delete meal
    public void deleteMeal(Long id) {

        mealRepository.deleteById(id);
    }
}
