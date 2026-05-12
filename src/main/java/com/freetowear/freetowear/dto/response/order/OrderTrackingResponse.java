package com.freetowear.freetowear.dto.response.order;

import com.freetowear.freetowear.entity.Order;
import com.freetowear.freetowear.enums.OrderStatus;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class OrderTrackingResponse {

    private String id;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String deliveryAddress;

    public OrderTrackingResponse(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.deliveryAddress = order.getDeliveryAddress().getStreet();
    }
}