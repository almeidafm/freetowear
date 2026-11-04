package com.freetowear.service;

import com.freetowear.dto.request.order.AddItemToCartRequest;
import com.freetowear.dto.request.order.FinishOrderRequest;
import com.freetowear.dto.response.coupon.CouponResponse;
import com.freetowear.dto.response.order.OrderResponse;
import com.freetowear.entity.*;
import com.freetowear.enums.*;
import com.freetowear.exception.BusinessException;
import com.freetowear.exception.ResourceNotFoundException;
import com.freetowear.infra.CloudinaryService;
import com.freetowear.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderTrackingRepository orderTrackingRepository;
    @Mock private CloudinaryService cloudinaryService;
    @InjectMocks private OrderService orderService;

    private Customer customer() {
        Customer customer = new Customer();
        customer.setId("customer-1");
        customer.setName("Maria");
        return customer;
    }

    private Product product(ProductVariation variation) {
        Product product = new Product();
        product.setId("product-1");
        product.setName("T-Shirt");
        product.setPrice(new BigDecimal("25.00"));
        product.setVariations(List.of(variation));
        variation.setId("variation-1");
        variation.setProduct(product);
        return product;
    }

    private Order cart(Customer customer) {
        Order order = new Order();
        order.setId("order-1");
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CART);
        order.setProductsValue(BigDecimal.ZERO);
        order.setShippingPrice(new BigDecimal("20.00"));
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    @Nested
    @DisplayName("addItem")
    class AddItemTests {
        @Test
        @DisplayName("Should add item and update cart total")
        void shouldAddItemWhenProductVariationExists() throws IOException {
            Customer customer = customer();
            ProductVariation variation = new ProductVariation();
            Product product = product(variation);
            Order cart = cart(customer);
            when(customerRepository.findById("customer-1")).thenReturn(Optional.of(customer));
            when(orderRepository.findByCustomerIdAndStatus("customer-1", OrderStatus.CART)).thenReturn(Optional.of(cart));
            when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArgument(0));

            orderService.addItem("customer-1", new AddItemToCartRequest("product-1", "variation-1", 2, null, "Print"));

            ArgumentCaptor<OrderItem> captor = ArgumentCaptor.forClass(OrderItem.class);
            verify(orderItemRepository).save(captor.capture());
            assertThat(captor.getValue().getQuantity()).isEqualTo(2);
            assertThat(captor.getValue().getSubtotal()).isEqualByComparingTo("50.00");
            assertThat(captor.getValue().getDescription()).isEqualTo("Print");
            assertThat(cart.getProductsValue()).isEqualByComparingTo("50.00");
            verify(orderRepository).save(cart);
        }

        @Test
        @DisplayName("Should throw when customer does not exist")
        void shouldThrowWhenCustomerDoesNotExist() {
            when(customerRepository.findById("missing")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> orderService.addItem("missing", new AddItemToCartRequest()))
                    .isInstanceOf(ResourceNotFoundException.class).hasMessage("Customer not found");
        }
    }

    @Nested
    @DisplayName("cart item operations")
    class CartItemTests {
        @Test
        @DisplayName("Should update quantity and recalculate cart total")
        void shouldUpdateItemQuantityWhenItemMatches() {
            Customer customer = customer(); Order order = cart(customer);
            ProductVariation variation = new ProductVariation(); variation.setId("variation-1");
            Product product = product(variation);
            OrderItem item = new OrderItem(); item.setProduct(product); item.setProductVariation(variation);
            item.setUnitPrice(new BigDecimal("25.00")); item.setSubtotal(new BigDecimal("25.00"));
            when(orderRepository.findByCustomerIdAndStatus("customer-1", OrderStatus.CART)).thenReturn(Optional.of(order));
            when(orderItemRepository.findAllByOrderId("order-1")).thenReturn(List.of(item));

            orderService.updateItemQuantity("customer-1", "product-1", "variation-1", 3);

            assertThat(item.getSubtotal()).isEqualByComparingTo("75.00");
            assertThat(order.getProductsValue()).isEqualByComparingTo("75.00");
            verify(orderItemRepository).save(item);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Should reject invalid quantity")
        void shouldRejectInvalidQuantity() {
            assertThatThrownBy(() -> orderService.updateItemQuantity("customer-1", "p", "v", 0))
                    .isInstanceOf(BusinessException.class).hasMessage("Quantity must be at least 1");
            verifyNoInteractions(orderRepository);
        }

        @Test
        @DisplayName("Should remove matching cart item and recalculate total")
        void shouldRemoveItemWhenItemMatches() {
            Customer customer = customer(); Order order = cart(customer);
            ProductVariation variation = new ProductVariation(); variation.setId("variation-1");
            Product product = product(variation);
            OrderItem item = new OrderItem(); item.setProduct(product); item.setProductVariation(variation);
            item.setSubtotal(new BigDecimal("25.00"));
            when(orderRepository.findByCustomerIdAndStatus("customer-1", OrderStatus.CART)).thenReturn(Optional.of(order));
            when(orderItemRepository.findAllByOrderId("order-1")).thenReturn(List.of(item), List.of());

            orderService.removeItem("customer-1", "product-1", "variation-1");

            verify(orderItemRepository).delete(item);
            assertThat(order.getProductsValue()).isEqualByComparingTo("0");
            verify(orderRepository).save(order);
        }
    }

    @Nested
    @DisplayName("order retrieval")
    class RetrievalTests {
        @Test
        @DisplayName("Should exclude cart orders and sort recent orders")
        void shouldReturnNonCartOrdersWhenGettingOrders() {
            Customer customer = customer();
            Order old = cart(customer); old.setStatus(OrderStatus.PAID); old.setCreatedAt(LocalDateTime.now().minusDays(2));
            Order recent = cart(customer); recent.setId("order-2"); recent.setStatus(OrderStatus.SHIPPED); recent.setCreatedAt(LocalDateTime.now());
            when(orderRepository.findAllByCustomerId("customer-1")).thenReturn(List.of(old, cart(customer), recent));
            when(orderItemRepository.findAllByOrderId(any())).thenReturn(List.of());
            when(orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc(any())).thenReturn(List.of());

            List<OrderResponse> result = orderService.getOrders("customer-1");

            assertThat(result).extracting(OrderResponse::getId).containsExactly("order-2", "order-1");
        }

        @Test
        @DisplayName("Should return empty order when customer does not own order or order is cart")
        void shouldReturnEmptyWhenOrderIsNotVisible() {
            Customer owner = customer(); Order order = cart(owner); order.setStatus(OrderStatus.PAID);
            when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

            assertThat(orderService.getOrder("order-1", "other-customer")).isEmpty();
            order.setStatus(OrderStatus.CART);
            assertThat(orderService.getOrder("order-1", "customer-1")).isEmpty();
        }

        @Test
        @DisplayName("Should throw when getting a missing order by ID")
        void shouldThrowWhenOrderDoesNotExist() {
            when(orderRepository.findById("missing")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> orderService.getOrderById("missing"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("tracking and cancellation")
    class TrackingTests {
        @Test
        @DisplayName("Should update tracking through a valid transition")
        void shouldUpdateTrackingWhenTransitionIsValid() {
            Order order = cart(customer());
            OrderTracking event = new OrderTracking(); event.setTrackingStatus(OrderTrackingStatus.SHIPPED);
            when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
            when(orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc("order-1")).thenReturn(List.of(event));

            orderService.updateOrderTracking("order-1", OrderTrackingStatus.IN_TRANSIT, "TRACK-1");

            assertThat(order.getTrackingCode()).isEqualTo("TRACK-1");
            verify(orderRepository).save(order);
            verify(orderTrackingRepository).save(any(OrderTracking.class));
        }

        @Test
        @DisplayName("Should reject invalid tracking transition")
        void shouldRejectInvalidTrackingTransition() {
            Order order = cart(customer()); OrderTracking event = new OrderTracking();
            event.setTrackingStatus(OrderTrackingStatus.SHIPPED);
            when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
            when(orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc("order-1")).thenReturn(List.of(event));

            assertThatThrownBy(() -> orderService.updateOrderTracking("order-1", OrderTrackingStatus.DELIVERED, null))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid tracking status transition");
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should cancel order before cancellation window")
        void shouldCancelOrderBeforeCancellationWindow() {
            Order order = cart(customer()); order.setStatus(OrderStatus.PAID);
            when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
            when(orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc("order-1")).thenReturn(List.of());

            orderService.cancelOrder("order-1");

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            verify(orderRepository).save(order);
            verify(orderTrackingRepository).save(any(OrderTracking.class));
        }
    }

    @Nested
    @DisplayName("coupon validation")
    class CouponTests {
        @Test
        @DisplayName("Should validate active coupon and map response")
        void shouldValidateCouponWhenCouponIsActive() {
            Coupon coupon = new Coupon(); coupon.setCode("SAVE10"); coupon.setActive(true);
            coupon.setStartDate(LocalDate.now().minusDays(1)); coupon.setEndDate(LocalDate.now().plusDays(1));
            when(couponRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));

            CouponResponse response = orderService.validateCoupon("customer-1", " SAVE10 ");

            assertThat(response.getCode()).isEqualTo("SAVE10");
            verify(couponRepository).findByCodeIgnoreCase("SAVE10");
        }

        @Test
        @DisplayName("Should reject blank coupon code")
        void shouldRejectBlankCouponCode() {
            assertThatThrownBy(() -> orderService.getValidCoupon("customer-1", " "))
                    .isInstanceOf(BusinessException.class).hasMessage("Coupon code is required");
        }

        @Test
        @DisplayName("Should reject inactive coupon")
        void shouldRejectInactiveCoupon() {
            Coupon coupon = new Coupon(); coupon.setActive(false);
            when(couponRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));
            assertThatThrownBy(() -> orderService.getValidCoupon("customer-1", "SAVE10"))
                    .isInstanceOf(BusinessException.class).hasMessage("Coupon is not active");
        }
    }
}