package com.homecook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.homecook.entity.Order;
import com.homecook.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    List<Order> findByUserAndHiddenFromCustomerFalseOrderByCreatedAtDesc(User user);

    List<Order> findAllByOrderByCreatedAtDesc();

}
