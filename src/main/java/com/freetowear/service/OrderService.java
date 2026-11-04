package com.freetowear.service;

import com.freetowear.dto.request.order.*;
import com.freetowear.dto.response.order.*;
import com.freetowear.dto.response.coupon.CouponResponse;
import com.freetowear.entity.Customer;
import com.freetowear.entity.Coupon;
import com.freetowear.entity.Address;
import com.freetowear.entity.Order;
import com.freetowear.entity.OrderItem;
import com.freetowear.entity.OrderTracking;
import com.freetowear.entity.Payment;
import com.freetowear.entity.Product;
import com.freetowear.entity.ProductVariation;
import com.freetowear.enums.DiscountType;
import com.freetowear.enums.OrderStatus;
import com.freetowear.enums.OrderTrackingStatus;
import com.freetowear.enums.PaymentStatus;
import com.freetowear.exception.BusinessException;
import com.freetowear.exception.ResourceNotFoundException;
import com.freetowear.infra.CloudinaryService;
import com.freetowear.repository.AddressRepository;
import com.freetowear.repository.CouponRepository;
import com.freetowear.repository.CustomerRepository;
import com.freetowear.repository.OrderItemRepository;
import com.freetowear.repository.OrderRepository;
import com.freetowear.repository.OrderTrackingRepository;
import com.freetowear.repository.PaymentRepository;
import com.freetowear.repository.ProductRepository;
import com.freetowear.util.UlidGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

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
    private OrderTrackingRepository orderTrackingRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private boolean isMatchingCartItem(OrderItem item, String productId, String variationId) {
        return item.getProduct().getId().equals(productId)
                && item.getProductVariation().getId().equals(variationId);
    }

    private Order createCart(Customer customer) {
        Order cart = new Order();

        cart.setCustomer(customer);
        cart.setStatus(OrderStatus.CART);
        cart.setProductsValue(BigDecimal.ZERO);
        cart.setShippingPrice(new BigDecimal("20.00"));

        return orderRepository.save(cart);
    }

    public void addItem(
            String idCustomer,
            AddItemToCartRequest request
    ) throws IOException {

        Customer customer = customerRepository.findById(idCustomer)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Order order = orderRepository
                .findByCustomerIdAndStatus(idCustomer, OrderStatus.CART)
                .orElseGet(() -> createCart(customer));

        Product product = productRepository.findById(request.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BusinessException("Product is not active");
        }
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new BusinessException("Quantity must be at least 1");
        }

        ProductVariation variation = Optional.ofNullable(product.getVariations()).orElse(List.of())
                .stream()
                .filter(productVariation ->
                        productVariation.getId().equals(request.getIdVariation()))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Variation not found for this product"));

        int availableStock = Optional.ofNullable(variation.getStock()).orElse(0);
        OrderItem existingItem = orderItemRepository.findAllByOrderId(order.getId()).stream()
                .filter(item -> isMatchingCartItem(item, product.getId(), variation.getId()))
                .findFirst().orElse(null);
        if (request.getQuantity() > availableStock) {
            throw new BusinessException("Insufficient stock");
        }

        OrderItem item = existingItem != null ? existingItem : new OrderItem();

        item.setOrder(order);
        item.setProduct(product);
        item.setProductVariation(variation);
        int newQuantity = (existingItem == null ? 0 : Optional.ofNullable(existingItem.getQuantity()).orElse(0))
                + request.getQuantity();
        item.setQuantity(newQuantity);
        item.setUnitPrice(product.getPrice());

        item.setSubtotal(
                product.getPrice()
                        .multiply(BigDecimal.valueOf(newQuantity))
        );

        boolean hasDescription = request.getDescription() != null;

        if (hasDescription) {
            item.setDescription(request.getDescription());
        }

        boolean hasCustomization =
                request.getCustomerCustomization() != null
                        && !request.getCustomerCustomization().isEmpty();

        String uploadedCustomizationId = null;
        if (hasCustomization) {
            uploadedCustomizationId = cloudinaryService.uploadPrivate(
                    request.getCustomerCustomization(),
                    "customization"
            );

            item.setCustomerCustomizationId(uploadedCustomizationId);
        }

        try {
            orderItemRepository.save(item);
        } catch (RuntimeException exception) {
            if (uploadedCustomizationId != null) cloudinaryService.delete(uploadedCustomizationId);
            throw exception;
        }

        variation.setStock(availableStock - request.getQuantity());

        BigDecimal currentProductsValue = Optional.ofNullable(order.getProductsValue())
                .orElse(BigDecimal.ZERO);
        order.setProductsValue(currentProductsValue.add(
                product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()))));
        if (order.getCoupon() != null) {
            order.setDiscountValue(calculateDiscount(order.getCoupon(), order.getProductsValue()));
        } else {
            order.setDiscountValue(BigDecimal.ZERO);
        }

        orderRepository.save(order);
    }

    @Transactional
    public void finishOrder(
            String customerId,
            FinishOrderRequest request
    ) {
        boolean hasPendingOrder = orderRepository
                .findByCustomerIdAndStatus(
                        customerId,
                        OrderStatus.PENDING
                )
                .isPresent();

        if (hasPendingOrder) {
            throw new BusinessException("Customer already has a pending order");
        }

        Order order = orderRepository
                .findByCustomerIdAndStatus(
                        customerId,
                        OrderStatus.CART
                )
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (orderItemRepository.findAllByOrderId(order.getId()).isEmpty()) {
            throw new BusinessException("Cart cannot be empty");
        }
        if (request.getMethod() == null) throw new BusinessException("Payment method is required");
        if (request.getInstallments() != null && (request.getInstallments() < 1 || request.getInstallments() > 12)) {
            throw new BusinessException("Installments must be between 1 and 12");
        }

        Address address = addressRepository
                .findById(request.getIdAddress())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        boolean addressBelongsToCustomer =
                address.getCustomer().getId().equals(customerId);

        if (!addressBelongsToCustomer) {
            throw new BusinessException("Address does not belong to customer");
        }

        order.setDeliveryAddress(address);

        if (request.getIdCoupon() != null && !request.getIdCoupon().isBlank()) {
            Coupon coupon = getValidCoupon(customerId, request.getIdCoupon());
            order.setCoupon(coupon);
        } else {
            order.setCoupon(null);
        }

        recalculateProductsValue(order);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(request.getMethod());
        payment.setAmountPaid(order.getTotalValue());
        payment.setStatus(PaymentStatus.PENDING);

        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BusinessException("Order already has a payment");
        }

        if (request.getInstallments() != null) {
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
                .filter(order -> order.getStatus() != OrderStatus.CART)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(order -> new OrderResponse(
                        order,
                        orderItemRepository.findAllByOrderId(order.getId()),
                        orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc(order.getId()),
                        cloudinaryService::buildUrl
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrder(String orderId, String customerId) {
        return orderRepository.findById(orderId)
                .filter(order -> order.getCustomer().getId().equals(customerId))
                .filter(order -> order.getStatus() != OrderStatus.CART)
                .map(order -> new OrderResponse(
                        order,
                        orderItemRepository.findAllByOrderId(order.getId()),
                        orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc(order.getId()),
                        cloudinaryService::buildUrl
                ));
    }

    public List<OrderResponse> getPendingOrders(String idCustomer) {
        return orderRepository
                .findAllByCustomerId(idCustomer)
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(OrderResponse::new)
                .toList();
    }

    public List<OrderResponse> getActiveOrders(String idCustomer) {
        return orderRepository
                .findAllByCustomerId(idCustomer)
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.SHIPPED)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(OrderResponse::new)
                .toList();
    }

    public List<OrderResponse> getRecentOrders(String idCustomer) {
        return orderRepository
                .findAllByCustomerId(idCustomer)
                .stream()
                .filter(o -> o.getStatus() != OrderStatus.CART)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(OrderResponse::new)
                .toList();
    }

    public Optional<Order> getPendingOrder(String customerId) {
        return orderRepository.findByCustomerIdAndStatus(
                customerId,
                OrderStatus.PENDING
        );
    }

    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        return new OrderResponse(order);
    }

    public OrderResponse getOrderById(String orderId, String customerId) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getCustomer() != null && Objects.equals(o.getCustomer().getId(), customerId))
                .filter(o -> o.getStatus() != OrderStatus.CART)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return new OrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderTrackingResponse getOrderTracking(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        return new OrderTrackingResponse(
                order,
                orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc(orderId)
        );
    }

    @Transactional(readOnly = true)
    public OrderTrackingResponse getOrderTracking(String orderId, String customerId) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getCustomer() != null && Objects.equals(o.getCustomer().getId(), customerId))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return new OrderTrackingResponse(order,
                orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc(orderId));
    }

    @Transactional
    public void updateOrderTracking(String orderId, OrderTrackingStatus trackingStatus, String trackingCode) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        List<OrderTracking> trackingEvents = orderTrackingRepository
                .findByOrderIdOrderByOccurredAtAsc(orderId);
        OrderTrackingStatus currentStatus = trackingEvents.isEmpty()
                ? null
                : trackingEvents.get(trackingEvents.size() - 1).getTrackingStatus();

        if (trackingStatus == null) throw new BusinessException("Tracking status is required");

        if (currentStatus == null) {
            throw new IllegalArgumentException("Order has no current tracking status");
        }

        boolean validTransition = switch (currentStatus) {
            case PAYMENT_RECEIVED -> trackingStatus == OrderTrackingStatus.PREPARING_ORDER;
            case PREPARING_ORDER -> trackingStatus == OrderTrackingStatus.READY_TO_SHIP;
            case READY_TO_SHIP -> trackingStatus == OrderTrackingStatus.SHIPPED;
            case SHIPPED -> trackingStatus == OrderTrackingStatus.IN_TRANSIT;
            case IN_TRANSIT -> trackingStatus == OrderTrackingStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> trackingStatus == OrderTrackingStatus.DELIVERED
                    || trackingStatus == OrderTrackingStatus.DELIVERY_ATTEMPTED;
            case DELIVERY_ATTEMPTED -> trackingStatus == OrderTrackingStatus.OUT_FOR_DELIVERY
                    || trackingStatus == OrderTrackingStatus.AWAITING_PICKUP;
            case AWAITING_PICKUP -> trackingStatus == OrderTrackingStatus.DELIVERED;
            default -> false;
        };

        if (!validTransition) {
            throw new IllegalArgumentException("Invalid tracking status transition");
        }

        if (trackingCode != null && !trackingCode.isBlank()) {
            order.setTrackingCode(trackingCode);
        }

        if (trackingStatus == OrderTrackingStatus.SHIPPED) {
            order.setStatus(OrderStatus.SHIPPED);
        } else if (trackingStatus == OrderTrackingStatus.DELIVERED) {
            order.setStatus(OrderStatus.DELIVERED);
        }

        orderRepository.save(order);

        saveTracking(order, trackingStatus);
    }

    @Transactional
    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled");
        }
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.ABANDONED) {
            throw new BusinessException("Order cannot be cancelled in its current state");
        }

        List<OrderTracking> trackingEvents = orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc(orderId);

        boolean isPastCancellationWindow = trackingEvents.stream().anyMatch(e ->
                e.getTrackingStatus() == OrderTrackingStatus.READY_TO_SHIP
                || e.getTrackingStatus() == OrderTrackingStatus.SHIPPED
                || e.getTrackingStatus() == OrderTrackingStatus.IN_TRANSIT
                || e.getTrackingStatus() == OrderTrackingStatus.OUT_FOR_DELIVERY
                || e.getTrackingStatus() == OrderTrackingStatus.DELIVERY_ATTEMPTED
                || e.getTrackingStatus() == OrderTrackingStatus.AWAITING_PICKUP
                || e.getTrackingStatus() == OrderTrackingStatus.DELIVERED
        );

        if (isPastCancellationWindow) {
            throw new BusinessException("Order cannot be cancelled once it is ready to ship or beyond");
        }

        order.setStatus(OrderStatus.CANCELLED);
        restoreStock(order);
        orderRepository.save(order);
        saveTracking(order, OrderTrackingStatus.CANCELLED);
    }

    @Transactional
    public void cancelOrder(String orderId, String customerId) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getCustomer() != null && Objects.equals(o.getCustomer().getId(), customerId))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        cancelOrder(orderId);
    }

    @Transactional
    public void updateItemQuantity(
            String idCustomer,
            String idProduct,
            String idVariation,
            Integer quantity
    ) {
        boolean invalidQuantity = quantity == null || quantity < 1;

        if (invalidQuantity) {
            throw new BusinessException("Quantity must be at least 1");
        }

        Order order = orderRepository
                .findByCustomerIdAndStatus(
                        idCustomer,
                        OrderStatus.CART
                )
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        OrderItem item = orderItemRepository.findAllByOrderId(order.getId())
                .stream()
                .filter(orderItem -> isMatchingCartItem(orderItem, idProduct, idVariation))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        ProductVariation variation = item.getProductVariation();
        int oldQuantity = Optional.ofNullable(item.getQuantity()).orElse(0);
        int currentStock = Optional.ofNullable(variation.getStock()).orElse(0);
        int quantityDelta = quantity - oldQuantity;
        if (quantityDelta > currentStock) {
            throw new BusinessException("Insufficient stock");
        }
        variation.setStock(currentStock - quantityDelta);
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
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        OrderItem item = orderItemRepository
                .findAllByOrderId(order.getId())
                .stream()
                .filter(orderItem -> isMatchingCartItem(orderItem, idProduct, idVariation))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        orderItemRepository.delete(item);

        restoreStock(item);

        recalculateProductsValue(order);
    }

    private void recalculateProductsValue(Order order) {

        BigDecimal productsValue = orderItemRepository
                .findAllByOrderId(order.getId())
                .stream()
                .map(item -> Optional.ofNullable(item.getSubtotal()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setProductsValue(productsValue);

        if (order.getCoupon() != null) {
            BigDecimal discount = calculateDiscount(order.getCoupon(), productsValue);
            order.setDiscountValue(discount);
        } else {
            order.setDiscountValue(BigDecimal.ZERO);
        }

        orderRepository.save(order);
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal productsValue) {
        if (coupon == null || productsValue == null || coupon.getDiscountValue() == null) {
            return BigDecimal.ZERO;
        }

        if (coupon.getDiscountType() == null || coupon.getDiscountValue().signum() < 0) {
            throw new BusinessException("Invalid coupon discount");
        }
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE
                && coupon.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("Percentage discount cannot exceed 100");
        }
        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = productsValue
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue();
        }

        if (discount.compareTo(productsValue) > 0) {
            discount = productsValue;
        }

        return discount;
    }

    @Transactional
    public void payOrder(String customerId) {
        Order order = orderRepository
                .findByCustomerIdAndStatus(customerId, OrderStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("No active order found for this customer"));

        order.setStatus(OrderStatus.PAID);
        order.setTrackingCode(generateTrackingCode());
        orderRepository.save(order);
        saveTracking(order, OrderTrackingStatus.PAYMENT_RECEIVED);
    }

    private String generateTrackingCode() {
        return UlidGenerator.generate();
    }

    private void saveTracking(Order order, OrderTrackingStatus status) {
        OrderTracking tracking = new OrderTracking();
        tracking.setOrder(order);
        tracking.setTrackingStatus(status);
        tracking.setTrackingCode(order.getTrackingCode());
        orderTrackingRepository.save(tracking);
    }

    public CouponResponse validateCoupon(String customerId, String code) {
        Coupon coupon = getValidCoupon(customerId, code);
        return new CouponResponse(coupon);
    }

    public Coupon getValidCoupon(String customerId, String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException("Coupon code is required");
        }

        String couponIdentifier = code.trim();
        Coupon coupon = couponRepository
                .findByCodeIgnoreCase(couponIdentifier)
                .or(() -> couponRepository.findById(couponIdentifier))
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        if (Boolean.FALSE.equals(coupon.getActive())) {
            throw new BusinessException("Coupon is not active");
        }

        validateCouponDiscount(coupon);

        LocalDate today = LocalDate.now();
        if (coupon.getStartDate() != null && today.isBefore(coupon.getStartDate())) {
            throw new BusinessException("Coupon is not yet valid");
        }

        if (coupon.getEndDate() != null && today.isAfter(coupon.getEndDate())) {
            throw new BusinessException("Coupon has expired");
        }

        if (coupon.getMinimumOrderValue() != null && customerId != null) {
            Order cart = orderRepository
                    .findByCustomerIdAndStatus(customerId, OrderStatus.CART)
                    .orElse(null);
            if (cart == null) throw new BusinessException("Cart not found");
            if (cart.getProductsValue() == null
                    || cart.getProductsValue().compareTo(coupon.getMinimumOrderValue()) < 0) {
                throw new BusinessException("Order value does not meet minimum order value for this coupon");
            }
        }

        return coupon;
    }

    private void validateCouponDiscount(Coupon coupon) {
        if (coupon.getDiscountType() == null || coupon.getDiscountValue() == null
                || coupon.getDiscountValue().signum() < 0
                || (coupon.getDiscountType() == DiscountType.PERCENTAGE
                && coupon.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new BusinessException("Invalid coupon discount");
        }
    }

    private void restoreStock(Order order) {
        orderItemRepository.findAllByOrderId(order.getId()).forEach(this::restoreStock);
    }

    private void restoreStock(OrderItem item) {
        if (item.getProductVariation() != null) {
            ProductVariation variation = item.getProductVariation();
            variation.setStock(Optional.ofNullable(variation.getStock()).orElse(0)
                    + Optional.ofNullable(item.getQuantity()).orElse(0));
        }
    }
}