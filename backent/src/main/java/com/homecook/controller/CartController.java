package com.homecook.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

import com.homecook.entity.Cart;
import com.homecook.entity.CartItem;
import com.homecook.entity.Meal;
import com.homecook.entity.User;
import com.homecook.repository.CartItemRepository;
import com.homecook.repository.CartRepository;
import com.homecook.repository.MealRepository;
import com.homecook.repository.UserRepository;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MealRepository mealRepository;
    private final UserRepository userRepository;

    public CartController(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            MealRepository mealRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.mealRepository = mealRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String cart(HttpSession session, Model model) {
        User user = getLoggedInCustomer(session);
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = cartRepository.findByUser(user).orElse(null);
        List<CartItem> cartItems = cart == null ? new ArrayList<>() : cart.getItems();

        double subtotal = cartItems.stream()
                .filter(item -> item != null && item.getMeal() != null)
                .mapToDouble(item -> {
                    Integer quantityValue = item.getQuantity();
                    int quantity = quantityValue == null ? 1 : quantityValue;

                    Double priceValue = item.getMeal() == null ? null : item.getMeal().getPrice();
                    double price = priceValue == null ? 0.0 : priceValue;
                    return price * quantity;
                })
                .sum();

        double deliveryFee = cartItems.isEmpty() ? 0.0 : 35.0;
        double tax = subtotal * 0.05;
        double grandTotal = subtotal + deliveryFee + tax;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("customer", user);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("tax", tax);
        model.addAttribute("grandTotal", grandTotal);

        return "cart";
    }

    @PostMapping("/add/{mealId}")
    public String addToCart(
            @PathVariable Long mealId,
            @RequestParam(defaultValue = "Regular") String spiceLevel,
            @RequestParam(defaultValue = "Regular") String oilLevel,
            @RequestParam(defaultValue = "Regular") String sugarLevel,
            @RequestParam(defaultValue = "Regular") String saltLevel,
            @RequestParam(defaultValue = "") String specialInstructions,
            HttpSession session) {

        User user = getLoggedInCustomer(session);
        if (user == null) {
            return "redirect:/login";
        }

        Meal meal = mealRepository
                .findById(mealId)
                .orElseThrow();

        Cart cart = cartRepository
                .findByUser(user)
                .orElseGet(() -> {

                    Cart newCart = new Cart();

                    newCart.setUser(user);

                    return cartRepository.save(newCart);
                });

        CartItem item = new CartItem();

        item.setCart(cart);
        item.setMeal(meal);
        item.setQuantity(1);
        item.setCustomizations(buildCustomization(spiceLevel, oilLevel, sugarLevel, saltLevel, specialInstructions));

        cartItemRepository.save(item);

        return "redirect:/cart";
    }

    @PostMapping("/remove/{cartItemId}")
    public String removeItem(@PathVariable Long cartItemId, HttpSession session) {
        User user = getLoggedInCustomer(session);
        if (user == null) {
            return "redirect:/login";
        }

        cartItemRepository.findById(cartItemId).ifPresent(item -> {
            Cart itemCart = item.getCart();
            if (itemCart != null && itemCart.getUser() != null && itemCart.getUser().getId() != null
                    && itemCart.getUser().getId().equals(user.getId())) {
                cartItemRepository.delete(item);
            }
        });

        return "redirect:/cart";
    }

    private User getLoggedInCustomer(HttpSession session) {
        Object sessionUser = session.getAttribute("loggedInUser");
        if (!(sessionUser instanceof User loggedInUser)
                || loggedInUser.getId() == null
                || !"CUSTOMER".equalsIgnoreCase(loggedInUser.getRole())) {
            return null;
        }
        return userRepository.findById(loggedInUser.getId()).orElse(null);
    }

    private String buildCustomization(String spice, String oil, String sugar, String salt, String instructions) {
        String safeInstructions = instructions == null ? "" : instructions.trim();
        if (safeInstructions.length() > 300) {
            safeInstructions = safeInstructions.substring(0, 300);
        }
        String customization = "Spice: " + safeChoice(spice) + ", Oil: " + safeChoice(oil)
                + ", Sugar: " + safeChoice(sugar) + ", Salt: " + safeChoice(salt);
        if (!safeInstructions.isBlank()) {
            customization += ", Other: " + safeInstructions;
        }
        return customization;
    }

    private String safeChoice(String choice) {
        if (choice == null) return "Regular";
        return switch (choice.toLowerCase()) {
            case "none" -> "None";
            case "less", "light" -> choice.substring(0, 1).toUpperCase() + choice.substring(1).toLowerCase();
            case "regular" -> "Regular";
            case "extra" -> "Extra";
            default -> "Regular";
        };
    }
}