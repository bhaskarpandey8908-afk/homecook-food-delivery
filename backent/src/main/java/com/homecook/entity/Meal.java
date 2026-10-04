package com.homecook.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Locale;

@Entity
@Table(name = "meals")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    private Double price;

    private String category;

    private String mealTime;

    private String foodType;

    private String image;

    private Integer quantity;

    private boolean available;

    private LocalDate menuDate;

    @ManyToOne
    @JoinColumn(name = "chef_id")
    private Chef chef;

    public Meal() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMealTime() {
        return mealTime;
    }

    public void setMealTime(String mealTime) {
        this.mealTime = mealTime;
    }

    public String getMealTimeLabel() {
        if ("Breakfast".equals(mealTime) || "Lunch".equals(mealTime) || "Dinner".equals(mealTime)) {
            return mealTime;
        }
        String details = ((name == null ? "" : name) + " "
                + (category == null ? "" : category)).toLowerCase(Locale.ROOT);
        if (details.matches(".*\\b(dosa|idli|uttapam|pancake|paratha|poha|upma|breakfast)\\b.*")) {
            return "Breakfast";
        }
        if (details.matches(".*\\b(biryani|supper|dinner|soup|roti|naan|tandoori)\\b.*")) {
            return "Dinner";
        }
        return "Lunch";
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getFoodTypeLabel() {
        if ("VEG".equalsIgnoreCase(foodType)) return "Veg";
        if ("NON_VEG".equalsIgnoreCase(foodType)) return "Non-Veg";

        String details = ((name == null ? "" : name) + " "
                + (description == null ? "" : description) + " "
                + (category == null ? "" : category)).toLowerCase(Locale.ROOT);
        return details.matches(".*\\b(chicken|mutton|lamb|beef|pork|fish|prawn|shrimp|seafood|egg|omelette|keema|meat)\\b.*")
                ? "Non-Veg" : "Veg";
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDate getMenuDate() {
        return menuDate;
    }

    public void setMenuDate(LocalDate menuDate) {
        this.menuDate = menuDate;
    }

    public Chef getChef() {
        return chef;
    }

    public void setChef(Chef chef) {
        this.chef = chef;
    }
}