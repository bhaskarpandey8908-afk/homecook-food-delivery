package com.homecook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.homecook.entity.OrderMessage;

public interface OrderMessageRepository extends JpaRepository<OrderMessage, Long> {

    List<OrderMessage> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);
}
