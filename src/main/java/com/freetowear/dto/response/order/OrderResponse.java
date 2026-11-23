package com.freetowear.dto.response.order;

import com.freetowear.entity.Order;
import com.freetowear.entity.OrderItem;
import com.freetowear.entity.OrderTracking;
import com.freetowear.enums.OrderStatus;
import com.freetowear.enums.OrderTrackingStatus;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

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
    private boolean canCancel;
    private boolean isPaid;

    public OrderResponse() {}

    public OrderResponse(Order order) {
        this(order, List.of());
    }

    public OrderResponse(Order order, List<OrderItem> items) {
        this(order, items, List.of());
    }

    public OrderResponse(Order order, List<OrderItem> items, List<OrderTracking> tracking) {
        this(order, items, tracking, productId -> null);
    }

    public OrderResponse(Order order, List<OrderItem> items, List<OrderTracking> tracking,
                         Function<String, String> imageUrlBuilder) {
        this.id = order.getId();
        this.customerName = order.getCustomer().getName();
        this.deliveryAddress = formatAddress(order);
        this.productsValue = order.getProductsValue();
        this.shippingPrice = order.getShippingPrice();
        this.discountValue = order.getDiscountValue();
        this.totalValue = order.getTotalValue();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.items = items.stream().map(item -> new OrderItemResponse(item, imageUrlBuilder)).toList();
        this.trackingCode = order.getTrackingCode();
        this.trackingEvents = tracking.stream()
                .map(OrderTrackingResponse.TrackingEventResponse::new)
                .toList();
        this.latestTrackingEvent = this.trackingEvents.isEmpty()
                ? null
                : this.trackingEvents.get(this.trackingEvents.size() - 1);
        this.isPaid = order.getStatus() == OrderStatus.PAID;
        this.canCancel = computeCanCancel(order.getStatus(), tracking);
    }

    private static String formatAddress(Order order) {
        if (order.getDeliveryAddress() == null) return null;
        var address = order.getDeliveryAddress();
        return address.getStreet() + ", " + address.getNumber() + " - "
                + address.getNeighborhood() + " - " + address.getCity() + "/"
                + address.getState() + " - CEP " + address.getCep();
    }

    private static final List<OrderTrackingStatus> NON_CANCELLABLE_TRACKING_STATUSES = List.of(
            OrderTrackingStatus.READY_TO_SHIP,
            OrderTrackingStatus.SHIPPED,
            OrderTrackingStatus.IN_TRANSIT,
            OrderTrackingStatus.OUT_FOR_DELIVERY,
            OrderTrackingStatus.DELIVERY_ATTEMPTED,
            OrderTrackingStatus.AWAITING_PICKUP,
            OrderTrackingStatus.DELIVERED
    );

    private static boolean computeCanCancel(OrderStatus status, List<OrderTracking> tracking) {
        if (status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED) return false;
        return tracking.stream()
                .noneMatch(e -> NON_CANCELLABLE_TRACKING_STATUSES.contains(e.getTrackingStatus()));
    }

    @Getter
    public static class OrderItemResponse {
        private final String idProduct;
        private final String idVariation;
        private final String productName;
        private final String imageUrl;
        private final String customizationImageUrl;
        private final String description;
        private final String color;
        private final String size;
        private final String variation;
        private final Integer quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal subtotal;
        private final Boolean customShirt;
        private final String customization;

        public OrderItemResponse(OrderItem item) {
            this(item, productId -> null);
        }

        public OrderItemResponse(OrderItem item, Function<String, String> imageUrlBuilder) {
            this.customShirt = Boolean.TRUE.equals(item.getCustomShirt());
            this.idProduct = customShirt ? "custom-shirt" : item.getProduct().getId();
            this.idVariation = customShirt ? "custom-shirt-" + item.getCustomColor() + "-" + item.getCustomSize() : item.getProductVariation().getId();
            this.productName = customShirt ? "Camiseta personalizada" : item.getProduct().getName();
            this.imageUrl = customShirt ? (item.getCustomizationImage() == null ? null : imageUrlBuilder.apply(item.getCustomizationImage())) : item.getProductVariation().getImages() == null
                    || item.getProductVariation().getImages().isEmpty()
                    ? null
                    : imageUrlBuilder.apply(item.getProductVariation().getImages().get(0).getImagePublicId());
            this.customizationImageUrl = item.getCustomizationImage() == null
                    ? null
                    : imageUrlBuilder.apply(item.getCustomizationImage());
            this.description = item.getDescription();
            this.color = customShirt ? item.getCustomColor() : item.getProductVariation().getColor();
            this.size = customShirt ? item.getCustomSize() : item.getProductVariation().getSize().name();
            this.variation = this.color + " / " + this.size;
            this.customization = item.getCustomizationData();
            this.quantity = item.getQuantity();
            this.unitPrice = item.getUnitPrice();
            this.subtotal = item.getSubtotal();
        }
    }
}
