package com.freetowear.freetowear.entity;

import com.freetowear.freetowear.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "orders", indexes = {
        @Index(columnList = "status"),
        @Index(columnList = "created_at"),
        @Index(columnList = "customer_id")
})
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id", nullable = false)
    private Address deliveryAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CART;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal productsValue;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalValue;

    @Column(length = 50)
    private String trackingCode;

    private LocalDate estimatedDelivery;

    // =====================
    // TOTAL CALCULATION
    // =====================
    private void calculateTotal() {
        if (productsValue != null && shippingPrice != null && discountValue != null) {
            this.totalValue = productsValue.subtract(discountValue).add(shippingPrice);
        }
    }

    @PrePersist
    @PreUpdate
    private void calculateTotalBeforeSave() {
        calculateTotal();
    }
}