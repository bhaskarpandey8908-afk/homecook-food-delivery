package com.homecook.controller;

import java.util.Optional;

import com.homecook.entity.Meal;
import com.homecook.entity.Order;
import com.homecook.entity.User;
import com.homecook.repository.MealRepository;
import com.homecook.repository.OrderRepository;
import com.homecook.repository.UserRepository;
import com.homecook.service.UserService;
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
import static org.mockito.Mockito.never;

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

    @MockBean
    private UserService userService;

    @Test
    void blockCustomer_shouldRedirectToDashboard() throws Exception {
        User owner = new User();
        owner.setRole("OWNER");
        mockMvc.perform(post("/owner/customers/1/block").sessionAttr("loggedInUser", owner))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner/dashboard"));
    }

    @Test
    void createOwner_withoutOwnerSession_redirectsToOwnerLogin() throws Exception {
        mockMvc.perform(post("/owner/owners").param("email", "new-owner@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner-login"));

        verify(userService, never()).register(any(User.class));
    }

    @Test
    void createOwner_withOwnerSession_setsOwnerRole() throws Exception {
        User owner = new User();
        owner.setRole("OWNER");
        when(userService.register(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/owner/owners")
                        .sessionAttr("loggedInUser", owner)
                        .param("name", "Second Owner")
                        .param("email", "second-owner@example.com")
                        .param("phone", "1234567890")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner/dashboard#owner-management"));

        org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userService).register(userCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("OWNER", userCaptor.getValue().getRole());
    }

    @Test
    void dashboard_withoutOwnerSession_redirectsToOwnerLogin() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/owner/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner-login"));
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
