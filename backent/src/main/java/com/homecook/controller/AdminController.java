package com.homecook.controller;

import com.homecook.repository.ChefRepository;
import com.homecook.repository.MealRepository;
import com.homecook.repository.OrderRepository;
import com.homecook.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ChefRepository chefRepository;
    private final MealRepository mealRepository;
    private final OrderRepository orderRepository;

    public AdminController(
            UserRepository userRepository,
            ChefRepository chefRepository,
            MealRepository mealRepository,
            OrderRepository orderRepository) {

        this.userRepository = userRepository;
        this.chefRepository = chefRepository;
        this.mealRepository = mealRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute(
                "users",
                userRepository.count()
        );

        model.addAttribute(
                "chefs",
                chefRepository.count()
        );

        model.addAttribute(
                "meals",
                mealRepository.count()
        );

        model.addAttribute(
                "orders",
                orderRepository.count()
        );

        return "admin-dashboard";
    }
}
