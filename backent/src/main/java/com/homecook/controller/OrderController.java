package com.homecook.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.homecook.entity.Order;
import com.homecook.entity.User;
import com.homecook.repository.OrderRepository;
import com.homecook.service.OrderService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private static final DateTimeFormatter CONFIRMATION_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public String customerOrders(HttpSession session, Model model) {
        User customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/login";
        }
        model.addAttribute("orders", orderRepository.findByUserAndHiddenFromCustomerFalseOrderByCreatedAtDesc(customer));
        return "customer-orders";
    }

    @PostMapping("/{orderId}/delivered")
    public String markDelivered(
            @PathVariable Long orderId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User customer = getLoggedInCustomer(session);
        if (customer == null) return "redirect:/login";

        Order order = orderService.getOrderById(orderId).orElse(null);
        if (order == null || order.getUser() == null || !customer.getId().equals(order.getUser().getId())) {
            return "redirect:/orders";
        }
        if (!"CANCELLED".equalsIgnoreCase(order.getOrderStatus())) {
            order.setOrderStatus("DELIVERED");
            orderRepository.save(order);
            redirectAttributes.addFlashAttribute("orderNotice", "Order marked as delivered.");
        }
        return "redirect:/orders";
    }

    @PostMapping("/{orderId}/remove")
    public String removeFromCustomerList(
            @PathVariable Long orderId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User customer = getLoggedInCustomer(session);
        if (customer == null) return "redirect:/login";

        Order order = orderService.getOrderById(orderId).orElse(null);
        if (order == null || order.getUser() == null || !customer.getId().equals(order.getUser().getId())) {
            return "redirect:/orders";
        }
        if (!"DELIVERED".equalsIgnoreCase(order.getOrderStatus())
                && !"CANCELLED".equalsIgnoreCase(order.getOrderStatus())) {
            redirectAttributes.addFlashAttribute("orderNotice", "You can remove an order from your list after it is delivered or cancelled.");
            return "redirect:/orders";
        }

        order.setHiddenFromCustomer(true);
        orderRepository.save(order);
        redirectAttributes.addFlashAttribute("orderNotice", "Order removed from your list. The owner can still view its history.");
        return "redirect:/orders";
    }

    @PostMapping("/place")
    public String placeOrder(
            @RequestParam String address,
            @RequestParam String deliveryType,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/login";
        }
        if (address == null || address.isBlank()) {
            redirectAttributes.addFlashAttribute("checkoutError", "Please enter a delivery address.");
            return "redirect:/cart";
        }

        try {
            Order order = orderService.placeOrder(customer.getId(), address.trim(), deliveryType);
            return "redirect:/orders/confirmation/" + order.getId();
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("checkoutError", "We could not place this order. Please check your cart and try again.");
            return "redirect:/cart";
        }
    }

    @GetMapping("/confirmation/{orderId}")
    public String confirmation(
            @PathVariable Long orderId,
            HttpSession session,
            Model model) {

        User customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(orderId).orElse(null);
        if (order == null || order.getUser() == null || !customer.getId().equals(order.getUser().getId())) {
            return "redirect:/meals";
        }

        model.addAttribute("order", order);
        model.addAttribute("orderNumber", String.format("HC-%06d", order.getId()));
        model.addAttribute("bookingDateTime", order.getCreatedAt() == null
                ? "Pending" : order.getCreatedAt().format(CONFIRMATION_DATE_FORMAT));
        return "order-confirmation";
    }

    private User getLoggedInCustomer(HttpSession session) {
        Object sessionUser = session.getAttribute("loggedInUser");
        if (sessionUser instanceof User customer
                && customer.getId() != null
                && "CUSTOMER".equalsIgnoreCase(customer.getRole())) {
            return customer;
        }
        return null;
    }
}
