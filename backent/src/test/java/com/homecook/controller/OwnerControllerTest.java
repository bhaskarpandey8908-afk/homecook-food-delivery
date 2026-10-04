package com.homecook.controller;

import java.util.Optional;

import com.homecook.entity.Meal;
import com.homecook.entity.Order;
import com.homecook.entity.User;
import com.homecook.repository.MealRepository;
import com.homecook.repository.OrderRepository;
import com.homecook.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(OwnerController.class)
@AutoConfigureMockMvc(addFilters = false)
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private MealRepository mealRepository;

    @Test
    void blockCustomer_shouldRedirectToDashboard() throws Exception {
        mockMvc.perform(post("/owner/customers/1/block"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner/dashboard"));
    }

    @Test
    void owner_canAddMealToMenu() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setRole("OWNER");

        mockMvc.perform(post("/owner/meals")
                        .sessionAttr("loggedInUser", owner)
                        .param("name", "Paneer Wrap")
                        .param("price", "180")
                        .param("mealTime", "Lunch")
                        .param("foodType", "VEG")
                        .param("quantity", "10")
                        .param("available", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner/dashboard#menu-management"));

        verify(mealRepository).save(any(Meal.class));
    }

    @Test
    void owner_canRemoveMealFromMenuWithoutDeletingIt() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setRole("OWNER");
        Meal meal = new Meal();
        meal.setAvailable(true);
        when(mealRepository.findById(5L)).thenReturn(Optional.of(meal));

        mockMvc.perform(post("/owner/meals/5/remove").sessionAttr("loggedInUser", owner))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner/dashboard#menu-management"));

        org.junit.jupiter.api.Assertions.assertFalse(meal.isAvailable());
        verify(mealRepository).save(meal);
    }

    @Test
    void owner_canMarkOrderDelivered() throws Exception {
        User owner = new User();
        owner.setRole("OWNER");
        Order order = new Order();
        order.setOrderStatus("PLACED");
        when(orderRepository.findById(11L)).thenReturn(Optional.of(order));

        mockMvc.perform(post("/owner/orders/11/delivered").sessionAttr("loggedInUser", owner))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner/dashboard#customer-orders"));

        org.junit.jupiter.api.Assertions.assertEquals("DELIVERED", order.getOrderStatus());
        org.junit.jupiter.api.Assertions.assertTrue(order.isHiddenFromCustomer());
        verify(orderRepository).save(order);
    }

    @Test
    void owner_canRemoveOrderFromDashboardWithoutDeletingHistory() throws Exception {
        User owner = new User();
        owner.setRole("OWNER");
        Order order = new Order();
        when(orderRepository.findById(12L)).thenReturn(Optional.of(order));

        mockMvc.perform(post("/owner/orders/12/remove").sessionAttr("loggedInUser", owner))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner/dashboard#customer-orders"));

        org.junit.jupiter.api.Assertions.assertTrue(order.isHiddenFromOwner());
        verify(orderRepository).save(order);
    }
}
