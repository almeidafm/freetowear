package com.freetowear.freetowear.service;

import com.freetowear.freetowear.dto.request.order.AddItemToOrderRequest;
import com.freetowear.freetowear.dto.request.order.CreateOrderRequest;
import com.freetowear.freetowear.dto.request.order.FinishOrderRequest;
import com.freetowear.freetowear.dto.response.order.OrderResponse;
import com.freetowear.freetowear.dto.response.order.OrderTrackingResponse;
import com.freetowear.freetowear.entity.*;
import com.freetowear.freetowear.enums.PaymentStatus;
import com.freetowear.freetowear.enums.OrderStatus;
import com.freetowear.freetowear.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProductVariationRepository variationRepository;

    public void createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getIdCustomer())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Address address = addressRepository.findById(request.getIdAddress())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Address does not belong to this customer");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setDeliveryAddress(address);

        if (request.getIdCoupon() != null) {
            couponRepository.findById(request.getIdCoupon())
                    .ifPresent(order::setCoupon);
        }

        order.setProductsValue(new BigDecimal("100.00"));
        order.setShippingPrice(new BigDecimal("20.00"));

        if (order.getCoupon() != null) {
            order.setDiscountValue(new BigDecimal("10.00"));
        }

        orderRepository.save(order);
    }

    public void addItem(Integer orderId, AddItemToOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Product product = productRepository.findById(request.getIdProduct())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductVariation variation = variationRepository.findById(request.getIdVariation())
                .orElseThrow(() -> new RuntimeException("Variation not found"));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setProductVariation(variation);
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(product.getPrice());

        orderItemRepository.save(item);
    }

    public void finishOrder(Integer orderId, FinishOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(request.getMethod());
        payment.setAmountPaid(order.getTotalValue());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaidAt(LocalDateTime.now());

        if (request.getInstallments() != null) {
            payment.setInstallments(request.getInstallments());
        }

        paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }

    public List<OrderResponse> getOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderResponse::new)
                .toList();
    }

    public OrderResponse getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return new OrderResponse(order);
    }

    public OrderTrackingResponse getOrderTracking(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return new OrderTrackingResponse(order);
    }

    public void cancelOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order cannot be cancelled in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}