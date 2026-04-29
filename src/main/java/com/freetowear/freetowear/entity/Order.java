package com.freetowear.freetowear.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
        @Index(columnList = "status"),
        @Index(columnList = "created_at"),
        @Index(columnList = "customer_id")
})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
    // GETTERS AND SETTERS
    // =====================

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getProductsValue() {
        return productsValue;
    }

    public void setProductsValue(BigDecimal productsValue) {
        this.productsValue = productsValue;
        calculateTotal();
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
        calculateTotal();
    }

    public BigDecimal getShippingPrice() {
        return shippingPrice;
    }

    public void setShippingPrice(BigDecimal shippingPrice) {
        this.shippingPrice = shippingPrice;
        calculateTotal();
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public LocalDate getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(LocalDate estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

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

    // =====================
    // ENUM
    // =====================
    public enum OrderStatus {
        CART,
        PENDING,
        PAID,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        ABANDONED
    }
}