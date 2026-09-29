package com.freetowear.dto.response.order;

import com.freetowear.entity.Order;
import com.freetowear.entity.OrderTracking;
import com.freetowear.enums.OrderStatus;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderTrackingResponse {

    private String id;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String deliveryAddress;
    private List<TrackingEventResponse> events;

    public OrderTrackingResponse(Order order, List<OrderTracking> tracking) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.deliveryAddress = order.getDeliveryAddress() != null
                ? order.getDeliveryAddress().getStreet() : null;
        this.events = tracking.stream().map(TrackingEventResponse::new).toList();
    }

    @Getter
    public static class TrackingEventResponse {
        private final String id;
        private final com.freetowear.enums.OrderTrackingStatus status;
        private final LocalDateTime occurredAt;
        private final String trackingCode;

        public TrackingEventResponse(OrderTracking tracking) {
            this.id = tracking.getId();
            this.status = tracking.getTrackingStatus();
            this.occurredAt = tracking.getOccurredAt();
            this.trackingCode = tracking.getTrackingCode();
        }
    }
}
