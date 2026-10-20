package com.freetowear.repository;

import com.freetowear.entity.OrderTracking;

import com.freetowear.enums.OrderTrackingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderTrackingRepository
        extends JpaRepository<OrderTracking, String> {

    List<OrderTracking> findByOrderIdOrderByOccurredAtAsc(String orderId);
    List<OrderTracking> findByTrackingStatusAndOccurredAtBetween(
            OrderTrackingStatus trackingStatus,
            LocalDateTime start,
            LocalDateTime end
    );
}