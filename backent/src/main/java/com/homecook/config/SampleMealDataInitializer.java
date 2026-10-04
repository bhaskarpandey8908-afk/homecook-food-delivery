package com.homecook.config;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.homecook.entity.Meal;
import com.homecook.repository.MealRepository;

/** Adds a starter menu only when the meal table is empty. */
@Component
public class SampleMealDataInitializer implements ApplicationRunner {

    private final MealRepository mealRepository;

    public SampleMealDataInitializer(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (mealRepository.count() > 0) {
            return;
        }

        List<Meal> starterMenu = List.of(
                meal("Homestyle Paneer Butter Masala", "Creamy tomato curry with soft paneer, served with fragrant rice.", 240.0, "Paneer & Curry", "Dinner", "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=900&q=80"),
                meal("Masala Dosa", "Crisp dosa filled with spiced potato, served with coconut chutney and sambar.", 160.0, "South Indian", "Breakfast", "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=900&q=80"),
                meal("Chicken Dum Biryani", "Aromatic basmati rice layered with spices and slow-cooked chicken.", 280.0, "Family Favorites", "Dinner", "https://images.unsplash.com/photo-1563379091339-03246963d96c?auto=format&fit=crop&w=900&q=80"),
                meal("Garden Fresh Millet Bowl", "A nourishing bowl of seasonal vegetables, greens and wholesome millets.", 210.0, "Healthy Bowls", "Lunch", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=900&q=80"),
                meal("Mumbai Pav Bhaji", "Buttery pav with a slow-cooked, masala-rich vegetable bhaji.", 150.0, "Street Food", "Lunch", "https://images.unsplash.com/photo-1606491956689-2ea866880c84?auto=format&fit=crop&w=900&q=80"),
                meal("Dal Tadka Rice", "Comforting yellow dal tempered with cumin, garlic and warming spices.", 175.0, "Comfort Food", "Lunch", "https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=900&q=80"),
                meal("Crispy Veg Samosa", "Golden pastry parcels filled with spiced potatoes and peas.", 90.0, "Street Food", "Breakfast", "https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=900&q=80"),
                meal("Idli Sambar Combo", "Soft steamed idlis with lentil sambar and fresh coconut chutney.", 130.0, "South Indian", "Breakfast", "https://images.unsplash.com/photo-1589302168068-964664d93dc0?auto=format&fit=crop&w=900&q=80")
        );

        mealRepository.saveAll(starterMenu);
    }

    private Meal meal(String name, String description, double price, String category, String mealTime, String image) {
        Meal meal = new Meal();
        meal.setName(name);
        meal.setDescription(description);
        meal.setPrice(price);
        meal.setCategory(category);
        meal.setMealTime(mealTime);
        meal.setImage(image);
        meal.setQuantity(30);
        meal.setAvailable(true);
        meal.setMenuDate(LocalDate.now());
        return meal;
    }
}
