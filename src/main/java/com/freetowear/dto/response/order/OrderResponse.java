package com.freetowear.dto.response.order;

import com.freetowear.entity.Order;
import com.freetowear.entity.OrderItem;
import com.freetowear.entity.OrderTracking;
import com.freetowear.enums.OrderStatus;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderResponse {

    private String id;
    private String customerName;
    private String deliveryAddress;
    private BigDecimal productsValue;
    private BigDecimal shippingPrice;
    private BigDecimal discountValue;
    private BigDecimal totalValue;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private String trackingCode;
    private List<OrderTrackingResponse.TrackingEventResponse> trackingEvents;
    private OrderTrackingResponse.TrackingEventResponse latestTrackingEvent;

    public OrderResponse() {}

    public OrderResponse(Order order) {
        this(order, List.of());
    }

    public OrderResponse(Order order, List<OrderItem> items) {
        this(order, items, List.of());
    }

    public OrderResponse(Order order, List<OrderItem> items, List<OrderTracking> tracking) {
        this.id = order.getId();
        this.customerName = order.getCustomer().getName();
        this.deliveryAddress = order.getDeliveryAddress() != null ? order.getDeliveryAddress().getStreet() : null;
        this.productsValue = order.getProductsValue();
        this.shippingPrice = order.getShippingPrice();
        this.discountValue = order.getDiscountValue();
        this.totalValue = order.getTotalValue();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.items = items.stream().map(OrderItemResponse::new).toList();
        this.trackingCode = order.getTrackingCode();
        this.trackingEvents = tracking.stream()
                .map(OrderTrackingResponse.TrackingEventResponse::new)
                .toList();
        this.latestTrackingEvent = this.trackingEvents.isEmpty()
                ? null
                : this.trackingEvents.get(this.trackingEvents.size() - 1);
    }

    @Getter
    public static class OrderItemResponse {
        private final String idProduct;
        private final String idVariation;
        private final String productName;
        private final String variation;
        private final Integer quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal subtotal;

        public OrderItemResponse(OrderItem item) {
            this.idProduct = item.getProduct().getId();
            this.idVariation = item.getProductVariation().getId();
            this.productName = item.getProduct().getName();
            this.variation = item.getProductVariation().getColor() + " / " + item.getProductVariation().getSize().name();
            this.quantity = item.getQuantity();
            this.unitPrice = item.getUnitPrice();
            this.subtotal = item.getSubtotal();
        }
    }
}
