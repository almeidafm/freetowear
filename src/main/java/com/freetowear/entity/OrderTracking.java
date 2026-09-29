package com.freetowear.entity;

import com.freetowear.enums.OrderTrackingStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.PrePersist;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "order_events",
        indexes = {
                @Index(columnList = "order_id"),
                @Index(columnList = "order_id, occurred_at"),
                @Index(columnList = "tracking_status"),
                @Index(columnList = "tracking_code")
        }
)
public class OrderTracking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_status", nullable = false, length = 50)
    private OrderTrackingStatus trackingStatus;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @PrePersist
    private void onCreate() {
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }
}
