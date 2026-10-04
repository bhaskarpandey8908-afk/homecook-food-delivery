package com.homecook.controller;

import com.homecook.entity.Chef;
import com.homecook.entity.Meal;
import com.homecook.repository.ChefRepository;
import com.homecook.repository.MealRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/chef")
public class ChefController {

    private final ChefRepository chefRepository;
    private final MealRepository mealRepository;

    public ChefController(
            ChefRepository chefRepository,
            MealRepository mealRepository) {

        this.chefRepository = chefRepository;
        this.mealRepository = mealRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute(
                "chefs",
                chefRepository.findAll()
        );

        return "chef-dashboard";
    }

    @PostMapping("/meal/add")
    public String addMeal(
            @ModelAttribute Meal meal,
            @RequestParam Long chefId) {

        Chef chef = chefRepository
                .findById(chefId)
                .orElseThrow();

        meal.setChef(chef);
        meal.setMenuDate(LocalDate.now());
        meal.setAvailable(true);

        mealRepository.save(meal);

        return "redirect:/meals";
    }
}
