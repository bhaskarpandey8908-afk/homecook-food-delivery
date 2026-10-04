package com.homecook.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.homecook.entity.Meal;
import com.homecook.entity.Order;
import com.homecook.entity.User;
import com.homecook.repository.MealRepository;
import com.homecook.repository.OrderRepository;
import com.homecook.repository.UserRepository;
import com.homecook.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final MealRepository mealRepository;
    private final UserService userService;

    public OwnerController(
            UserRepository userRepository,
            OrderRepository orderRepository,
            MealRepository mealRepository,
            UserService userService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.mealRepository = mealRepository;
        this.userService = userService;
    }

    @PostMapping("/owners")
    public String createOwner(
            @ModelAttribute User newOwner,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isOwner(session)) return "redirect:/owner-login";

        newOwner.setRole("OWNER");
        try {
            userService.register(newOwner);
            redirectAttributes.addFlashAttribute("ownerSuccess", "Owner account created. They can now sign in on the owner login page.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("ownerError", "Could not create the owner account. Check the required fields and make sure the email is not already registered.");
        }
        return "redirect:/owner/dashboard#owner-management";
    }

    @PostMapping("/meals")
    public String addMeal(
            @ModelAttribute Meal formMeal,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isOwner(session)) return "redirect:/owner-login";
        if (!isValidMeal(formMeal)) {
            redirectAttributes.addFlashAttribute("mealError", "Enter a meal name and a price greater than zero.");
            return "redirect:/owner/dashboard#menu-management";
        }

        Meal meal = new Meal();
        copyMenuFields(formMeal, meal);
        meal.setMenuDate(LocalDate.now());
        mealRepository.save(meal);
        redirectAttributes.addFlashAttribute("mealSuccess", "Meal added to the menu.");
        return "redirect:/owner/dashboard#menu-management";
    }

    @PostMapping("/meals/{id}/update")
    public String updateMeal(
            @PathVariable Long id,
            @ModelAttribute Meal formMeal,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isOwner(session)) return "redirect:/owner-login";
        Meal meal = mealRepository.findById(id).orElse(null);
        if (meal == null) {
            redirectAttributes.addFlashAttribute("mealError", "That meal could not be found.");
            return "redirect:/owner/dashboard#menu-management";
        }
        if (!isValidMeal(formMeal)) {
            redirectAttributes.addFlashAttribute("mealError", "Enter a meal name and a price greater than zero.");
            return "redirect:/owner/dashboard#menu-management";
        }

        copyMenuFields(formMeal, meal);
        mealRepository.save(meal);
        redirectAttributes.addFlashAttribute("mealSuccess", "Meal details updated.");
        return "redirect:/owner/dashboard#menu-management";
    }

    @PostMapping("/meals/{id}/remove")
    public String removeMeal(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isOwner(session)) return "redirect:/owner-login";
        mealRepository.findById(id).ifPresent(meal -> {
            meal.setAvailable(false);
            mealRepository.save(meal);
        });
        redirectAttributes.addFlashAttribute("mealSuccess", "Meal removed from the customer menu. Past orders are preserved.");
        return "redirect:/owner/dashboard#menu-management";
    }

    @PostMapping("/meals/{id}/restore")
    public String restoreMeal(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isOwner(session)) return "redirect:/owner-login";
        mealRepository.findById(id).ifPresent(meal -> {
            meal.setAvailable(true);
            mealRepository.save(meal);
        });
        redirectAttributes.addFlashAttribute("mealSuccess", "Meal is available on the customer menu again.");
        return "redirect:/owner/dashboard#menu-management";
    }

    private boolean isOwner(HttpSession session) {
        Object user = session.getAttribute("loggedInUser");
        return user instanceof User loggedInUser
                && ("OWNER".equalsIgnoreCase(loggedInUser.getRole()) || "ADMIN".equalsIgnoreCase(loggedInUser.getRole()));
    }

    private boolean isValidMeal(Meal meal) {
        return meal.getName() != null && !meal.getName().isBlank()
                && meal.getPrice() != null && meal.getPrice() > 0;
    }

    private void copyMenuFields(Meal source, Meal target) {
        target.setName(source.getName().trim());
        target.setDescription(source.getDescription());
        target.setPrice(source.getPrice());
        target.setCategory(source.getCategory());
        target.setMealTime(source.getMealTime());
        target.setFoodType("NON_VEG".equalsIgnoreCase(source.getFoodType()) ? "NON_VEG" : "VEG");
        target.setImage(source.getImage());
        target.setQuantity(source.getQuantity() == null ? 0 : Math.max(0, source.getQuantity()));
        target.setAvailable(source.isAvailable());
    }

    @PostMapping("/customers/{id}/block")
    public String blockCustomer(@PathVariable Long id, HttpSession session) {
        if (!isOwner(session)) return "redirect:/owner-login";
        userRepository.findById(id).ifPresent(user -> {
            user.setBlocked(!user.isBlocked());
            userRepository.save(user);
        });
        return "redirect:/owner/dashboard";
    }

    @PostMapping("/customers/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, HttpSession session) {
        if (!isOwner(session)) return "redirect:/owner-login";
        userRepository.deleteById(id);
        return "redirect:/owner/dashboard";
    }

    @PostMapping("/orders/{id}/delivered")
    public String markOrderDelivered(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isOwner(session)) return "redirect:/owner-login";
        orderRepository.findById(id).ifPresent(order -> {
            if (!"CANCELLED".equalsIgnoreCase(order.getOrderStatus())) {
                order.setOrderStatus("DELIVERED");
                order.setHiddenFromCustomer(true);
                orderRepository.save(order);
            }
        });
        redirectAttributes.addFlashAttribute("orderActionNotice", "Order marked delivered and removed from the customer's active list. It remains in customer order history.");
        return "redirect:/owner/dashboard#customer-orders";
    }

    @PostMapping("/orders/{id}/remove")
    public String removeOrderFromDashboard(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isOwner(session)) return "redirect:/owner-login";
        orderRepository.findById(id).ifPresent(order -> {
            order.setHiddenFromOwner(true);
            orderRepository.save(order);
        });
        redirectAttributes.addFlashAttribute("orderActionNotice", "Order removed from this dashboard list. Historical records are preserved.");
        return "redirect:/owner/dashboard#customer-orders";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (!isOwner(session)) return "redirect:/owner-login";

        List<User> customers = userRepository.findByRoleOrderByIdDesc("CUSTOMER");
        List<Order> allOrders = orderRepository.findAllByOrderByCreatedAtDesc();
        List<Order> orders = allOrders.stream().filter(order -> !order.isHiddenFromOwner()).toList();
        List<Meal> meals = mealRepository.findAll();
        meals.forEach(meal -> meal.setMealTime(meal.getMealTimeLabel()));
        Map<Long, List<Order>> ordersByCustomer = new HashMap<>();
        customers.forEach(customer -> ordersByCustomer.put(customer.getId(), new ArrayList<>()));
        allOrders.stream()
            .filter(order -> order.getUser() != null && order.getUser().getId() != null)
            .forEach(order -> ordersByCustomer
                .computeIfAbsent(order.getUser().getId(), ignored -> new ArrayList<>())
                .add(order));

        double totalRevenue = allOrders.stream()
                .mapToDouble(order -> {
                    Double totalValue = order.getTotalAmount();
                    return totalValue == null ? 0.0 : totalValue;
                })
                .sum();

        model.addAttribute("customers", customers);
        model.addAttribute("meals", meals);
        model.addAttribute("orders", orders);
        model.addAttribute("ordersByCustomer", ordersByCustomer);
        model.addAttribute("customerCount", customers.size());
        model.addAttribute("orderCount", allOrders.size());
        model.addAttribute("totalRevenue", totalRevenue);

        return "owner-dashboard";
    }
}