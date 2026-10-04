package com.homecook.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.homecook.entity.Cart;
import com.homecook.entity.CartItem;
import com.homecook.entity.User;
import com.homecook.repository.CartItemRepository;
import com.homecook.repository.CartRepository;
import com.homecook.repository.MealRepository;
import com.homecook.repository.UserRepository;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @MockitoBean
    private MealRepository mealRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void removeItem_shouldDeleteCartItemAndRedirectToCart() throws Exception {
        User customer = new User();
        customer.setId(7L);
        customer.setRole("CUSTOMER");

        Cart cart = new Cart();
        cart.setUser(customer);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setQuantity(1);

        when(userRepository.findById(7L)).thenReturn(Optional.of(customer));
        when(cartItemRepository.findById(3L)).thenReturn(Optional.of(item));

        mockMvc.perform(post("/cart/remove/3")
                .sessionAttr("loggedInUser", customer))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

            verify(cartItemRepository).delete(item);
    }
}
