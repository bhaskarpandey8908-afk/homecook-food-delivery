package com.homecook.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homecook.entity.Cart;
import com.homecook.entity.CartItem;
import com.homecook.entity.Order;
import com.homecook.entity.OrderItem;
import com.homecook.entity.User;
import com.homecook.repository.CartRepository;
import com.homecook.repository.OrderRepository;
import com.homecook.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    // Place order
    @Transactional
    public Order placeOrder(
            Long userId,
            String address,
            String deliveryType) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Cart cart = cartRepository
                .findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {

            throw new RuntimeException(
                    "Cart is empty");
        }

        Order order = new Order();

        order.setUser(user);
        order.setDeliveryAddress(address);
        order.setDeliveryType(deliveryType);

        order.setOrderStatus("PLACED");
        order.setPaymentStatus("PENDING");

        double total = 0;

        for (CartItem cartItem : cart.getItems()) {

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setMeal(cartItem.getMeal());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(
                    cartItem.getMeal().getPrice()
            );
            orderItem.setCustomizations(cartItem.getCustomizations());

            order.getItems().add(orderItem);

            total +=
                    cartItem.getMeal().getPrice()
                    * cartItem.getQuantity();
        }

        double deliveryFee = 35.0;
        double tax = total * 0.05;
        order.setTotalAmount(total + deliveryFee + tax);

        Order savedOrder =
                orderRepository.save(order);

        // Empty cart after successful order
        cart.getItems().clear();

        cartRepository.save(cart);

        return savedOrder;
    }

    // Get order by ID
    public Optional<Order> getOrderById(Long id) {

        return orderRepository.findById(id);
    }

    // Get user's orders
    public List<Order> getUserOrders(Long userId) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return orderRepository
                .findByUserOrderByCreatedAtDesc(user);
    }

    // Update order status
    public Order updateOrderStatus(
            Long orderId,
            String status) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        order.setOrderStatus(status);

        return orderRepository.save(order);
    }

    // Update payment status
    public Order updatePaymentStatus(
            Long orderId,
            String paymentStatus) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        order.setPaymentStatus(paymentStatus);

        return orderRepository.save(order);
    }

    // Cancel order
    public Order cancelOrder(Long orderId) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        order.setOrderStatus("CANCELLED");

        return orderRepository.save(order);
    }

    // Get all orders - Admin
    public List<Order> getAllOrders() {

        return orderRepository.findAll();
    }
}
