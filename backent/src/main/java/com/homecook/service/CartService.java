package com.homecook.service;

import com.homecook.entity.Cart;
import com.homecook.entity.CartItem;
import com.homecook.entity.Meal;
import com.homecook.entity.User;
import com.homecook.repository.CartItemRepository;
import com.homecook.repository.CartRepository;
import com.homecook.repository.MealRepository;
import com.homecook.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MealRepository mealRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            MealRepository mealRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.mealRepository = mealRepository;
        this.userRepository = userRepository;
    }

    // Get or create cart
    public Cart getCart(Long userId) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return cartRepository
                .findByUser(user)
                .orElseGet(() -> {

                    Cart cart = new Cart();

                    cart.setUser(user);

                    return cartRepository.save(cart);
                });
    }

    // Add meal to cart
    public Cart addToCart(
            Long userId,
            Long mealId,
            int quantity) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Meal meal = mealRepository
                .findById(mealId)
                .orElseThrow(() ->
                        new RuntimeException("Meal not found"));

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
        item.setQuantity(quantity);

        cartItemRepository.save(item);

        return cart;
    }

    // Update cart item
    public CartItem updateQuantity(
            Long cartItemId,
            int quantity) {

        CartItem item = cartItemRepository
                .findById(cartItemId)
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found"));

        item.setQuantity(quantity);

        return cartItemRepository.save(item);
    }

    // Remove item
    public void removeItem(Long cartItemId) {

        cartItemRepository.deleteById(cartItemId);
    }

    // Clear cart
    public void clearCart(Long userId) {

        Cart cart = getCart(userId);

        cart.getItems().clear();

        cartRepository.save(cart);
    }

    // Calculate total
    public double calculateTotal(Long userId) {

        Cart cart = getCart(userId);

        double total = 0;

        for (CartItem item : cart.getItems()) {

            total +=
                    item.getMeal().getPrice()
                    * item.getQuantity();
        }

        return total;
    }
}