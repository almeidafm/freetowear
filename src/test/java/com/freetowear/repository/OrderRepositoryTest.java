package com.freetowear.repository;

import com.freetowear.entity.Customer;
import com.freetowear.entity.Order;
import com.freetowear.enums.OrderStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryTest extends RepositoryTestBase {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnOrdersForCustomerId() {
        Customer customer = persistCustomer("customer@example.com");
        Customer otherCustomer = persistCustomer("other@example.com");
        Order expected = persistOrder(customer, OrderStatus.PAID, LocalDateTime.now(), "TRACK-1");
        persistOrder(otherCustomer, OrderStatus.PAID, LocalDateTime.now(), "TRACK-2");

        flushAndClear();

        List<Order> result = orderRepository.findAllByCustomerId(customer.getId());

        assertThat(result).extracting(Order::getId).containsExactly(expected.getId());
    }

    @Test
    void shouldReturnEmptyWhenCustomerIdDoesNotExist() {
        persistOrder(persistCustomer("customer@example.com"), OrderStatus.PAID, LocalDateTime.now(), "TRACK-1");
        flushAndClear();

        assertThat(orderRepository.findAllByCustomerId("01J00000000000000000000000")).isEmpty();
    }

    @Test
    void shouldReturnOrderForCustomerAndStatus() {
        Customer customer = persistCustomer("customer@example.com");
        Order expected = persistOrder(customer, OrderStatus.PAID, LocalDateTime.now(), "TRACK-1");
        persistOrder(customer, OrderStatus.CART, LocalDateTime.now(), "TRACK-2");
        flushAndClear();

        assertThat(orderRepository.findByCustomerIdAndStatus(customer.getId(), OrderStatus.PAID))
                .get().extracting(Order::getId).isEqualTo(expected.getId());
    }

    @Test
    void shouldReturnEmptyWhenCustomerHasNoOrderWithStatus() {
        Customer customer = persistCustomer("customer@example.com");
        persistOrder(customer, OrderStatus.CART, LocalDateTime.now(), "TRACK-1");
        flushAndClear();

        assertThat(orderRepository.findByCustomerIdAndStatus(customer.getId(), OrderStatus.PAID)).isEmpty();
    }

    @Test
    void shouldReturnOrdersForCustomerEntity() {
        Customer customer = persistCustomer("customer@example.com");
        Order expected = persistOrder(customer, OrderStatus.PAID, LocalDateTime.now(), "TRACK-1");
        flushAndClear();

        assertThat(orderRepository.findByCustomer(customer)).extracting(Order::getId)
                .containsExactly(expected.getId());
    }

    @Test
    void shouldReturnOrdersAtInclusiveCreatedAtBoundaries() {
        Customer customer = persistCustomer("customer@example.com");
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 31, 23, 59);
        Order first = persistOrder(customer, OrderStatus.PAID, start, "TRACK-1");
        Order last = persistOrder(customer, OrderStatus.PAID, end, "TRACK-2");
        persistOrder(customer, OrderStatus.PAID, start.minusSeconds(1), "TRACK-3");
        persistOrder(customer, OrderStatus.PAID, end.plusSeconds(1), "TRACK-4");
        flushAndClear();

        assertThat(orderRepository.findByCreatedAtBetween(start, end)).extracting(Order::getId)
                .containsExactlyInAnyOrder(first.getId(), last.getId());
    }

    @Test
    void shouldReturnOrdersCreatedAtOrAfterBoundary() {
        Customer customer = persistCustomer("customer@example.com");
        LocalDateTime boundary = LocalDateTime.of(2026, 2, 1, 0, 0);
        Order atBoundary = persistOrder(customer, OrderStatus.PAID, boundary, "TRACK-1");
        Order after = persistOrder(customer, OrderStatus.PAID, boundary.plusSeconds(1), "TRACK-2");
        persistOrder(customer, OrderStatus.PAID, boundary.minusSeconds(1), "TRACK-3");
        flushAndClear();

        assertThat(orderRepository.findByCreatedAtGreaterThanEqual(boundary)).extracting(Order::getId)
                .containsExactlyInAnyOrder(atBoundary.getId(), after.getId());
    }

    @Test
    void shouldReturnTrueWhenTrackingCodeStartsWithPrefix() {
        persistOrder(persistCustomer("customer@example.com"), OrderStatus.PAID, LocalDateTime.now(), "ABC-123");
        flushAndClear();

        assertThat(orderRepository.existsByTrackingCodeStartingWith("ABC")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTrackingCodeDoesNotStartWithPrefix() {
        persistOrder(persistCustomer("customer@example.com"), OrderStatus.PAID, LocalDateTime.now(), "ABC-123");
        flushAndClear();

        assertThat(orderRepository.existsByTrackingCodeStartingWith("XYZ")).isFalse();
    }

    private Customer persistCustomer(String email) {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail(email);
        customer.setPassword("password");
        customer.setTermsAndConditionsAcceptedAt(LocalDateTime.now());
        entityManager.persist(customer);
        return customer;
    }

    private Order persistOrder(Customer customer, OrderStatus status, LocalDateTime createdAt, String trackingCode) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(status);
        order.setCreatedAt(createdAt);
        order.setProductsValue(new BigDecimal("100.00"));
        order.setDiscountValue(BigDecimal.ZERO);
        order.setShippingPrice(new BigDecimal("10.00"));
        order.setTrackingCode(trackingCode);
        entityManager.persist(order);
        return order;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}