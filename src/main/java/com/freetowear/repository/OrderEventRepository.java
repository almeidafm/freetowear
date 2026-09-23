package com.freetowear.repository;

import com.freetowear.entity.OrderEvent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderEventRepository
        extends JpaRepository<OrderEvent, String> {

    List<OrderEvent> findByOrderIdOrderByOccurredAtAsc(String orderId);
}