package com.freetowear.freetowear.service;

import com.freetowear.freetowear.dto.request.order.AddItemToOrderRequest;
import com.freetowear.freetowear.dto.request.order.CreateOrderRequest;
import com.freetowear.freetowear.dto.request.order.FinishOrderRequest;
import com.freetowear.freetowear.entity.*;
import com.freetowear.freetowear.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaidAt(LocalDateTime.now());

        if (request.getInstallments() != null) {
            payment.setInstallments(request.getInstallments());
        }

        paymentRepository.save(payment);

        order.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(order);
    }
}