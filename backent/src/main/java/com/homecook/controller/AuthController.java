package com.homecook.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.homecook.entity.User;
import com.homecook.service.DailyMenuRecommendationService;
import com.homecook.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UserService userService;
    private final DailyMenuRecommendationService dailyMenuRecommendationService;

    public AuthController(UserService userService, DailyMenuRecommendationService dailyMenuRecommendationService) {
        this.userService = userService;
        this.dailyMenuRecommendationService = dailyMenuRecommendationService;
    }

    @GetMapping({"/login", "/customer-login"})
    public String customerLoginPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("featuredMeals", dailyMenuRecommendationService.getRecommendedMeals());
        model.addAttribute("dailyMenuTheme", dailyMenuRecommendationService.getTodayTheme());
        return "login";
    }

    @GetMapping("/owner-login")
    public String ownerLoginPage(Model model) {
        model.addAttribute("user", new User());
        return "owner-login";
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String loginUser(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = userService.login(email, password);

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password.");
            return "redirect:/login";
        }

        session.setAttribute("loggedInUser", user);

        if ("OWNER".equalsIgnoreCase(user.getRole()) || "ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/owner/dashboard";
        }

        return "redirect:/meals";
    }

    @PostMapping(value = "/owner-login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String ownerLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = userService.loginByRole(email, password, "OWNER");

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Owner login failed. Please use a valid owner account.");
            return "redirect:/owner-login";
        }

        session.setAttribute("loggedInUser", user);
        return "redirect:/owner/dashboard";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> registerUserJson(@RequestBody User user) {
        applyPublicRegistrationRole(user);
        User savedUser = userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String registerUserForm(@ModelAttribute User user) {
        applyPublicRegistrationRole(user);
        userService.register(user);
        return "redirect:/login";
    }

    private void applyPublicRegistrationRole(User user) {
        if (user != null && !"CHEF".equalsIgnoreCase(user.getRole())) {
            user.setRole("CUSTOMER");
        }
    }
}