package com.freetowear.freetowear.dto.response.order;

import com.freetowear.freetowear.entity.Order;
import com.freetowear.freetowear.enums.OrderStatus;
import java.time.LocalDateTime;

public class OrderTrackingResponse {

    private Integer id;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String deliveryAddress;

    public OrderTrackingResponse(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.deliveryAddress = order.getDeliveryAddress().getStreet();
    }

    // getters...

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
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
}