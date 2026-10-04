package com.homecook.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.homecook.entity.Order;
import com.homecook.entity.OrderMessage;
import com.homecook.entity.User;
import com.homecook.repository.OrderMessageRepository;
import com.homecook.repository.OrderRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderMessageController {

    private final OrderRepository orderRepository;
    private final OrderMessageRepository orderMessageRepository;

    public OrderMessageController(OrderRepository orderRepository, OrderMessageRepository orderMessageRepository) {
        this.orderRepository = orderRepository;
        this.orderMessageRepository = orderMessageRepository;
    }

    @PostMapping("/owner/orders/{orderId}/messages")
    public String sendOrderMessage(
            @PathVariable Long orderId,
            @RequestParam String message,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User sender = getSessionUser(session);
        if (sender == null || !("OWNER".equalsIgnoreCase(sender.getRole()) || "ADMIN".equalsIgnoreCase(sender.getRole()))) {
            return "redirect:/owner-login";
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        String cleanMessage = message == null ? "" : message.trim();
        if (order == null || order.getUser() == null || cleanMessage.isBlank()) {
            redirectAttributes.addFlashAttribute("messageError", "Order or message is missing.");
            return "redirect:/owner/dashboard#order-" + orderId;
        }
        if (cleanMessage.length() > 1000) {
            cleanMessage = cleanMessage.substring(0, 1000);
        }

        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setCustomer(order.getUser());
        orderMessage.setOrder(order);
        orderMessage.setMessage(cleanMessage);
        orderMessageRepository.save(orderMessage);

        redirectAttributes.addFlashAttribute("messageSent", "Message sent to " + order.getUser().getName() + ".");
        return "redirect:/owner/dashboard#order-" + orderId;
    }

    @GetMapping("/customer/messages")
    public String customerMessages(HttpSession session, Model model) {
        User customer = getSessionUser(session);
        if (customer == null || !"CUSTOMER".equalsIgnoreCase(customer.getRole())) {
            return "redirect:/login";
        }

        List<OrderMessage> messages = orderMessageRepository.findByCustomer_IdOrderByCreatedAtDesc(customer.getId());
        model.addAttribute("messages", messages);
        return "customer-messages";
    }

    private User getSessionUser(HttpSession session) {
        Object sessionUser = session.getAttribute("loggedInUser");
        return sessionUser instanceof User user ? user : null;
    }
}
