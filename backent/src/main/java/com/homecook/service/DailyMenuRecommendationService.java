package com.homecook.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.homecook.entity.Meal;
import com.homecook.repository.MealRepository;

/**
 * A lightweight, local recommendation engine that ranks available meals using
 * the daily theme and meal category/name. It needs no external AI credentials.
 */
@Service
public class DailyMenuRecommendationService {

    private static final List<String> DAILY_THEMES = List.of(
            "Comfort food", "South Indian", "Healthy bowls", "Paneer & curry",
            "Street food", "Chef specials", "Family favorites");

    private final MealRepository mealRepository;

    public DailyMenuRecommendationService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    public String getTodayTheme() {
        return DAILY_THEMES.get(LocalDate.now().getDayOfWeek().getValue() - 1);
    }

    public List<Meal> getRecommendedMeals() {
        String theme = getTodayTheme().toLowerCase(Locale.ROOT);
        List<Meal> meals = mealRepository.findByAvailableTrue();
        if (meals.isEmpty()) {
            return List.of();
        }

        List<Meal> rankedMeals = meals.stream()
            .sorted(Comparator
                .comparingInt((Meal meal) -> score(meal, theme)).reversed()
                .thenComparing(Meal::getId, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        int dailyOffset = (int) Math.floorMod(LocalDate.now().toEpochDay(), rankedMeals.size());
        return java.util.stream.IntStream.range(0, Math.min(4, rankedMeals.size()))
            .mapToObj(index -> rankedMeals.get((dailyOffset + index) % rankedMeals.size()))
            .toList();
    }

    private int score(Meal meal, String theme) {
        String category = meal.getCategory() == null ? "" : meal.getCategory().toLowerCase(Locale.ROOT);
        String name = meal.getName() == null ? "" : meal.getName().toLowerCase(Locale.ROOT);
        int score = category.contains(theme) ? 10 : 0;

        if (theme.contains("south indian") && containsAny(name, "dosa", "idli", "uttapam", "sambar")) score += 5;
        if (theme.contains("healthy") && containsAny(name, "salad", "bowl", "grilled", "millet")) score += 5;
        if (theme.contains("paneer") && containsAny(name, "paneer", "curry", "masala")) score += 5;
        if (theme.contains("street") && containsAny(name, "chaat", "roll", "pav", "samosa")) score += 5;
        if (theme.contains("family") && containsAny(name, "thali", "biryani", "combo")) score += 5;
        if (theme.contains("comfort") && containsAny(name, "dal", "khichdi", "rice", "curry")) score += 5;

        return score;
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) {
            if (value.contains(term)) return true;
        }
        return false;
    }

}
