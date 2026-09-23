package com.freetowear.service;

import com.freetowear.dto.request.order.*;
import com.freetowear.dto.response.order.*;
import com.freetowear.dto.response.coupon.CouponResponse;
import com.freetowear.entity.Customer;
import com.freetowear.entity.Coupon;
import com.freetowear.entity.Address;
import com.freetowear.entity.Order;
import com.freetowear.entity.OrderItem;
import com.freetowear.entity.Payment;
import com.freetowear.entity.Product;
import com.freetowear.entity.ProductVariation;
import com.freetowear.enums.DiscountType;
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
import java.math.RoundingMode;
import java.time.LocalDate;
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
            throw new RuntimeException("Customer already has a pending order");
        }

        Order order = orderRepository
                .findByCustomerIdAndStatus(
                        customerId,
                        OrderStatus.CART
                )
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Address address = addressRepository
                .findById(request.getIdAddress())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        boolean addressBelongsToCustomer =
                address.getCustomer().getId().equals(customerId);

        if (!addressBelongsToCustomer) {
            throw new RuntimeException("Address does not belong to customer");
        }

        order.setDeliveryAddress(address);

        recalculateProductsValue(order);

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
                .map(order -> new OrderResponse(order, orderItemRepository.findAllByOrderId(order.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrder(String orderId, String customerId) {
        return orderRepository.findById(orderId)
                .filter(order -> order.getCustomer().getId().equals(customerId))
                .filter(order -> order.getStatus() != OrderStatus.CART)
                .map(order -> new OrderResponse(order, orderItemRepository.findAllByOrderId(order.getId())));
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

        BigDecimal discount = BigDecimal.ZERO;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = productsValue
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (coupon.getDiscountType() == DiscountType.FIXED) {
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
                .orElseThrow(() -> new RuntimeException("No active order found for this customer"));

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }

    public CouponResponse validateCoupon(String customerId, String code) {
        Coupon coupon = getValidCoupon(customerId, code);
        return new CouponResponse(coupon);
    }

    public Coupon getValidCoupon(String customerId, String code) {
        if (code == null || code.isBlank()) {
            throw new RuntimeException("Coupon code is required");
        }

        String couponIdentifier = code.trim();
        Coupon coupon = couponRepository
                .findByCodeIgnoreCase(couponIdentifier)
                .or(() -> couponRepository.findById(couponIdentifier))
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (Boolean.FALSE.equals(coupon.getActive())) {
            throw new RuntimeException("Coupon is not active");
        }

        LocalDate today = LocalDate.now();
        if (coupon.getStartDate() != null && today.isBefore(coupon.getStartDate())) {
            throw new RuntimeException("Coupon is not yet valid");
        }

        if (coupon.getEndDate() != null && today.isAfter(coupon.getEndDate())) {
            throw new RuntimeException("Coupon has expired");
        }

        if (coupon.getMinimumOrderValue() != null && customerId != null) {
            Order cart = orderRepository
                    .findByCustomerIdAndStatus(customerId, OrderStatus.CART)
                    .orElse(null);
            if (cart != null && cart.getProductsValue() != null
                    && cart.getProductsValue().compareTo(coupon.getMinimumOrderValue()) < 0) {
                throw new RuntimeException("Order value does not meet minimum order value for this coupon");
            }
        }

        return coupon;
    }
}