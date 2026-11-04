package com.freetowear.repository;

import com.freetowear.entity.Customer;
import com.freetowear.entity.Order;
import com.freetowear.entity.OrderTracking;
import com.freetowear.enums.OrderStatus;
import com.freetowear.enums.OrderTrackingStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class OrderTrackingRepositoryTest extends RepositoryTestBase {

    @Autowired
    private OrderTrackingRepository orderTrackingRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnEmptyWhenOrderHasNoTrackingEvents() {
        Order order = persistOrder("customer@mail.com");
        flushAndClear();

        assertThat(orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc(order.getId())).isEmpty();
    }

    @Test
    void shouldReturnOnlyTrackingEventsBelongingToRequestedOrder() {
        Order order = persistOrder("customer@mail.com");
        Order otherOrder = persistOrder("other@mail.com");
        OrderTracking expected = persistTracking(order, OrderTrackingStatus.SHIPPED,
                LocalDateTime.of(2026, 1, 2, 12, 0));
        persistTracking(otherOrder, OrderTrackingStatus.SHIPPED, LocalDateTime.of(2026, 1, 2, 12, 0));
        flushAndClear();

        assertThat(orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc(order.getId()))
                .extracting(OrderTracking::getId).containsExactly(expected.getId());
    }

    @Test
    void shouldReturnMultipleTrackingEventsInOccurredAtAscendingOrder() {
        Order order = persistOrder("customer@mail.com");
        OrderTracking later = persistTracking(order, OrderTrackingStatus.SHIPPED,
                LocalDateTime.of(2026, 1, 3, 12, 0));
        OrderTracking earlier = persistTracking(order, OrderTrackingStatus.PAYMENT_RECEIVED,
                LocalDateTime.of(2026, 1, 1, 12, 0));
        OrderTracking middle = persistTracking(order, OrderTrackingStatus.PREPARING_ORDER,
                LocalDateTime.of(2026, 1, 2, 12, 0));
        flushAndClear();

        assertThat(orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc(order.getId()))
                .extracting(OrderTracking::getId)
                .containsExactly(earlier.getId(), middle.getId(), later.getId());
    }

    @Test
    void shouldReturnAllEventsWithIdenticalTimestamps() {
        Order order = persistOrder("customer@mail.com");
        LocalDateTime occurredAt = LocalDateTime.of(2026, 1, 1, 12, 0);
        OrderTracking first = persistTracking(order, OrderTrackingStatus.PAYMENT_RECEIVED, occurredAt);
        OrderTracking second = persistTracking(order, OrderTrackingStatus.PREPARING_ORDER, occurredAt);
        flushAndClear();

        assertThat(orderTrackingRepository.findByOrderIdOrderByOccurredAtAsc(order.getId()))
                .extracting(OrderTracking::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void shouldReturnTrackingEventsByStatusWithinInclusiveTimeBoundaries() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 28, 23, 59);
        Order order = persistOrder("customer@mail.com");
        OrderTracking atStart = persistTracking(order, OrderTrackingStatus.DELIVERED, start);
        OrderTracking atEnd = persistTracking(order, OrderTrackingStatus.DELIVERED, end);
        persistTracking(order, OrderTrackingStatus.DELIVERED, start.minusSeconds(1));
        persistTracking(order, OrderTrackingStatus.DELIVERED, end.plusSeconds(1));
        flushAndClear();

        assertThat(orderTrackingRepository.findByTrackingStatusAndOccurredAtBetween(
                OrderTrackingStatus.DELIVERED, start, end))
                .extracting(OrderTracking::getId).containsExactlyInAnyOrder(atStart.getId(), atEnd.getId());
    }

    @Test
    void shouldFilterTrackingEventsByRequestedStatus() {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 2, 10, 12, 0);
        OrderTracking delivered = persistTracking(persistOrder("customer@mail.com"),
                OrderTrackingStatus.DELIVERED, occurredAt);
        persistTracking(persistOrder("other@mail.com"), OrderTrackingStatus.SHIPPED, occurredAt);
        flushAndClear();

        assertThat(orderTrackingRepository.findByTrackingStatusAndOccurredAtBetween(
                OrderTrackingStatus.DELIVERED, occurredAt.minusDays(1), occurredAt.plusDays(1)))
                .extracting(OrderTracking::getId).containsExactly(delivered.getId());
    }

    @Test
    void shouldReturnEmptyWhenNoTrackingEventsMatchStatusAndTimeRange() {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 1, 10, 12, 0);
        persistTracking(persistOrder("customer@mail.com"), OrderTrackingStatus.SHIPPED, occurredAt);
        flushAndClear();

        assertThat(orderTrackingRepository.findByTrackingStatusAndOccurredAtBetween(
                OrderTrackingStatus.DELIVERED, LocalDateTime.of(2026, 2, 1, 0, 0),
                LocalDateTime.of(2026, 3, 1, 0, 0))).isEmpty();
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

    private Order persistOrder(String email) {
        Order order = new Order();
        order.setCustomer(persistCustomer(email));
        order.setStatus(OrderStatus.PAID);
        order.setCreatedAt(LocalDateTime.now());
        order.setProductsValue(new BigDecimal("100.00"));
        order.setDiscountValue(BigDecimal.ZERO);
        order.setShippingPrice(new BigDecimal("10.00"));
        entityManager.persist(order);
        return order;
    }

    private OrderTracking persistTracking(Order order, OrderTrackingStatus status, LocalDateTime occurredAt) {
        OrderTracking tracking = new OrderTracking();
        tracking.setOrder(order);
        tracking.setTrackingStatus(status);
        tracking.setOccurredAt(occurredAt);
        entityManager.persist(tracking);
        return tracking;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}