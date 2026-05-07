package com.freetowear.freetowear.dto.response.order;

import com.freetowear.freetowear.entity.Order;
import com.freetowear.freetowear.enums.OrderStatus;
import java.math.BigDecimal;

public class OrderResponse {

    private Integer id;
    private String customerName;
    private String deliveryAddress;
    private BigDecimal productsValue;
    private BigDecimal shippingPrice;
    private BigDecimal discountValue;
    private BigDecimal totalValue;
    private OrderStatus status;

    public OrderResponse() {}

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.customerName = order.getCustomer().getName();
        this.deliveryAddress = order.getDeliveryAddress().getStreet();
        this.productsValue = order.getProductsValue();
        this.shippingPrice = order.getShippingPrice();
        this.discountValue = order.getDiscountValue();
        this.totalValue = order.getTotalValue();
        this.status = order.getStatus();
    }

    public Integer getId() { return id; }

    public String getCustomerName() {
        return customerName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public BigDecimal getProductsValue() {
        return productsValue;
    }

    public BigDecimal getShippingPrice() {
        return shippingPrice;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public OrderStatus getStatus() {
        return status;
    }
}