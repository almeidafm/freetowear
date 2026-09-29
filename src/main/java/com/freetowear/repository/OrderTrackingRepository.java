package com.freetowear.repository;

import com.freetowear.entity.OrderTracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTrackingRepository
        extends JpaRepository<OrderTracking, String> {

    List<OrderTracking> findByOrderIdOrderByOccurredAtAsc(String orderId);
}