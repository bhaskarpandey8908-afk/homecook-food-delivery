package com.homecook.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.homecook.entity.Meal;
import com.homecook.entity.User;
import com.homecook.repository.MealRepository;
import com.homecook.repository.OrderMessageRepository;
import com.homecook.service.DailyMenuRecommendationService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MealController {

    private final MealRepository mealRepository;
    private final OrderMessageRepository orderMessageRepository;
    private final DailyMenuRecommendationService dailyMenuRecommendationService;

    public MealController(MealRepository mealRepository, OrderMessageRepository orderMessageRepository,
            DailyMenuRecommendationService dailyMenuRecommendationService) {
        this.mealRepository = mealRepository;
        this.orderMessageRepository = orderMessageRepository;
        this.dailyMenuRecommendationService = dailyMenuRecommendationService;
    }

    @GetMapping("/meals")
    public String meals(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"CUSTOMER".equalsIgnoreCase(loggedInUser.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Please log in as a customer to view meals.");
            return "redirect:/login";
        }

        List<Meal> meals = mealRepository.findByAvailableTrue();
        meals.forEach(meal -> {
            meal.setMealTime(getMealTime(meal));
            meal.setFoodType(getFoodType(meal));
        });
        List<Meal> dailyMenuMeals = dailyMenuRecommendationService.getRecommendedMeals();
        dailyMenuMeals.forEach(meal -> {
            meal.setMealTime(getMealTime(meal));
            meal.setFoodType(getFoodType(meal));
        });
        model.addAttribute("dailyMenuMeals", dailyMenuMeals);
        model.addAttribute("dailyMenuTheme", dailyMenuRecommendationService.getTodayTheme());
        model.addAttribute("breakfastMeals", meals.stream().filter(meal -> "Breakfast".equals(getMealTime(meal))).toList());
        model.addAttribute("lunchMeals", meals.stream().filter(meal -> "Lunch".equals(getMealTime(meal))).toList());
        model.addAttribute("dinnerMeals", meals.stream().filter(meal -> "Dinner".equals(getMealTime(meal))).toList());
        model.addAttribute("orderMessages",
            orderMessageRepository.findByCustomer_IdOrderByCreatedAtDesc(loggedInUser.getId()));

        return "meals";
    }

    private String getFoodType(Meal meal) {
        return "Non-Veg".equals(meal.getFoodTypeLabel()) ? "NON_VEG" : "VEG";
    }

    private String getMealTime(Meal meal) {
        return meal.getMealTimeLabel();
    }

    @GetMapping("/meal/{id}")
    public String mealDetails(
            @PathVariable Long id,
            Model model) {

        Meal meal = mealRepository
                .findById(id)
                .orElse(null);

        model.addAttribute("meal", meal);

        return "meal-details";
    }
}