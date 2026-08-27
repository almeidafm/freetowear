package com.freetowear.service;

import com.freetowear.dto.request.order.AddItemToOrderRequest;
import com.freetowear.dto.request.order.CreateOrderRequest;
import com.freetowear.dto.request.order.FinishOrderRequest;
import com.freetowear.dto.response.order.OrderResponse;
import com.freetowear.dto.response.order.OrderTrackingResponse;
import com.freetowear.entity.Address;
import com.freetowear.entity.Customer;
import com.freetowear.entity.Order;
import com.freetowear.entity.OrderItem;
import com.freetowear.entity.Payment;
import com.freetowear.entity.Product;
import com.freetowear.entity.ProductVariation;
import com.freetowear.enums.OrderStatus;
import com.freetowear.enums.PaymentStatus;
import com.freetowear.infra.CloudinaryService;
import com.freetowear.repository.AddressRepository;
import com.freetowear.repository.CouponRepository;
import com.freetowear.repository.CustomerRepository;
import com.freetowear.repository.OrderItemRepository;
import com.freetowear.repository.OrderRepository;
import com.freetowear.repository.PaymentRepository;
import com.freetowear.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

    private boolean isMatchingCartItem(OrderItem item, String productId, String variationId) {
        return item.getProduct().getId().equals(productId)
                && item.getProductVariation().getId().equals(variationId);
    }

    public void createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getIdCustomer())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Address address = addressRepository.findById(request.getIdAddress())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        boolean addressBelongsToCustomer =
                address.getCustomer().getId().equals(customer.getId());

        if (!addressBelongsToCustomer) {
            throw new RuntimeException("Address does not belong to this customer");
        }

        Order order = orderRepository
                .findByCustomerIdAndStatus(
                        customer.getId(),
                        OrderStatus.CART
                )
                .orElseThrow(() -> new RuntimeException("No active cart found"));

        order.setDeliveryAddress(address);

        if (request.getIdCoupon() != null) {
            couponRepository.findById(request.getIdCoupon())
                    .ifPresent(order::setCoupon);
        } else {
            order.setCoupon(null);
        }

        orderRepository.save(order);
    }

    private Order createCart(Customer customer) {
        Order order = new Order();

        order.setCustomer(customer);
        order.setStatus(OrderStatus.CART);
        order.setProductsValue(BigDecimal.ZERO);
        order.setShippingPrice(new BigDecimal("20.00"));

        return orderRepository.save(order);
    }

    public void addItem(
            String idCustomer,
            AddItemToOrderRequest request
    ) throws IOException {

        Customer customer = customerRepository.findById(idCustomer)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Order order = orderRepository
                .findByCustomerIdAndStatus(idCustomer, OrderStatus.CART)
                .orElseGet(() -> createCart(customer));

        Product product = productRepository.findById(request.getIdProduct())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductVariation variation = product.getVariations()
                .stream()
                .filter(productVariation ->
                        productVariation.getId().equals(request.getIdVariation()))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Variation not found for this product"));

        OrderItem item = new OrderItem();

        item.setOrder(order);
        item.setProduct(product);
        item.setProductVariation(variation);
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(product.getPrice());

        item.setSubtotal(
                product.getPrice()
                        .multiply(BigDecimal.valueOf(request.getQuantity()))
        );

        boolean hasDescription = request.getDescription() != null;

        if (hasDescription) {
            item.setDescription(request.getDescription());
        }

        boolean hasCustomization =
                request.getCustomerCustomization() != null
                        && !request.getCustomerCustomization().isEmpty();

        if (hasCustomization) {
            String privateId = cloudinaryService.uploadPrivate(
                    request.getCustomerCustomization(),
                    "customization"
            );

            item.setCustomerCustomizationId(privateId);
        }

        orderItemRepository.save(item);

        order.setProductsValue(
                order.getProductsValue().add(item.getSubtotal())
        );

        orderRepository.save(order);
    }

    public void finishOrder(
            String orderId,
            FinishOrderRequest request
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setMethod(request.getMethod());
        payment.setAmountPaid(order.getTotalValue());
        payment.setStatus(PaymentStatus.PENDING);

        boolean hasInstallments = request.getInstallments() != null;

        if (hasInstallments) {
            payment.setInstallments(request.getInstallments());
        }

        paymentRepository.save(payment);

        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Optional<OrderResponse> getCart(String idCustomer) {
        return orderRepository
                .findByCustomerIdAndStatus(idCustomer, OrderStatus.CART)
                .map(order ->
                        new OrderResponse(
                                order,
                                orderItemRepository.findAllByOrderId(order.getId())
                        )
                );
    }

    public List<OrderResponse> getOrders(String idCustomer) {
        return orderRepository
                .findAllByCustomerId(idCustomer)
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

        boolean isOrderAlreadyFinalized = order.getStatus() == OrderStatus.PAID
                || order.getStatus() == OrderStatus.CANCELLED;

        if (isOrderAlreadyFinalized) {
            throw new RuntimeException("Order cannot be cancelled in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional
    public void updateItemQuantity(
            String idCustomer,
            String idProduct,
            String idVariation,
            Integer quantity
    ) {
        boolean invalidQuantity = quantity == null || quantity < 1;

        if (invalidQuantity) {throw new RuntimeException("Quantity must be at least 1");}

        Order order = orderRepository
                .findByCustomerIdAndStatus(
                        idCustomer,
                        OrderStatus.CART
                )
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        OrderItem item = orderItemRepository.findAllByOrderId(order.getId())
                .stream()
                .filter(orderItem -> isMatchingCartItem(orderItem, idProduct, idVariation))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        item.setQuantity(quantity);

        item.setSubtotal(
                item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(quantity))
        );

        orderItemRepository.save(item);

        recalculateProductsValue(order);
    }

    @Transactional
    public void removeItem(
            String idCustomer,
            String idProduct,
            String idVariation
    ) {
        Order order = orderRepository
                .findByCustomerIdAndStatus(
                        idCustomer,
                        OrderStatus.CART
                )
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        OrderItem item = orderItemRepository
                .findAllByOrderId(order.getId())
                .stream()
                .filter(orderItem -> isMatchingCartItem(orderItem, idProduct, idVariation))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        orderItemRepository.delete(item);

        recalculateProductsValue(order);
    }

    private void recalculateProductsValue(Order order) {

        BigDecimal productsValue = orderItemRepository
                .findAllByOrderId(order.getId())
                .stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setProductsValue(productsValue);

        orderRepository.save(order);
    }
}