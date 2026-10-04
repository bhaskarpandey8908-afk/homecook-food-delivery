package com.homecook.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.homecook.entity.User;
import com.homecook.service.DailyMenuRecommendationService;
import com.homecook.service.UserService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private DailyMenuRecommendationService dailyMenuRecommendationService;

    @Test
    void loginPage_displaysDailyMenu() throws Exception {
        when(dailyMenuRecommendationService.getRecommendedMeals()).thenReturn(List.of());
        when(dailyMenuRecommendationService.getTodayTheme()).thenReturn("South Indian");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void register_withJsonPayload_shouldSucceed() throws Exception {
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Test User");
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole("USER");

        when(userService.register(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"password123\",\"phone\":\"9999999999\",\"address\":\"Test Address\",\"role\":\"USER\"}"))
                .andExpect(status().isCreated());
    }
}
