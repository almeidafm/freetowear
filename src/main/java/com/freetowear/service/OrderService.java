package com.freetowear.service;

import com.freetowear.entity.*;
import com.freetowear.entity.*;
import com.freetowear.enums.PaymentStatus;
import com.freetowear.enums.OrderStatus;
import com.freetowear.infra.CloudinaryService;
import com.freetowear.repository.*;
import com.freetowear.repository.*;
import com.freetowear.dto.request.order.AddItemToOrderRequest;
import com.freetowear.dto.request.order.CreateOrderRequest;
import com.freetowear.dto.request.order.FinishOrderRequest;
import com.freetowear.dto.response.order.OrderResponse;
import com.freetowear.dto.response.order.OrderTrackingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
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
    private CloudinaryService cloudinaryService;

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
        order.setStatus(OrderStatus.CART);

        if (request.getIdCoupon() != null) {
            couponRepository.findById(request.getIdCoupon())
                    .ifPresent(order::setCoupon);
        }

        order.setProductsValue(BigDecimal.ZERO);
        order.setShippingPrice(new BigDecimal("20.00"));

        orderRepository.save(order);
    }

    public void addItem(String idCustomer, AddItemToOrderRequest request) throws IOException {
        Order order = orderRepository.findByCustomerIdAndStatus(idCustomer, OrderStatus.CART)
                .orElseThrow(() -> new RuntimeException("No active cart found"));

        Product product = productRepository.findById(request.getIdProduct())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductVariation variation = product.getVariations().stream()
                .filter(productVariation -> productVariation.getId().equals(request.getIdVariation()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Variation not found for this product"));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setProductVariation(variation);
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(product.getPrice());
        item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));

        if (request.getDescription() != null)
            item.setDescription(request.getDescription());

        if (request.getCustomerCustomization() != null && !request.getCustomerCustomization().isEmpty()) {
            String privateId = cloudinaryService.uploadPrivate(
                    request.getCustomerCustomization(),
                    "customization"
            );
            item.setCustomerCustomizationId(privateId);
        }

        orderItemRepository.save(item);

        BigDecimal total = orderItemRepository.findAllByOrderId(order.getId())
                .stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setProductsValue(total);
        orderRepository.save(order);
    }

    public void finishOrder(String orderId, FinishOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(request.getMethod());
        payment.setAmountPaid(order.getTotalValue());
        payment.setStatus(PaymentStatus.PENDING);

        if (request.getInstallments() != null) {
            payment.setInstallments(request.getInstallments());
        }

        paymentRepository.save(payment);

        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
    }

    public OrderResponse getCart(String idCustomer) {
        return orderRepository.findByCustomerIdAndStatus(idCustomer, OrderStatus.CART)
                .map(OrderResponse::new)
                .orElseThrow(() -> new RuntimeException("No active cart found"));
    }

    public List<OrderResponse> getOrders(String idCustomer) {
        return orderRepository.findAllByCustomerId(idCustomer)
                .stream()
                .map(OrderResponse::new)
                .toList();
    }

    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return new OrderResponse(order);
    }

    public OrderTrackingResponse getOrderTracking(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return new OrderTrackingResponse(order);
    }

    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order cannot be cancelled in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}