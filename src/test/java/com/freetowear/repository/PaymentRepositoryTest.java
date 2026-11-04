package com.freetowear.repository;

import com.freetowear.entity.Customer;
import com.freetowear.entity.Order;
import com.freetowear.entity.Payment;
import com.freetowear.enums.OrderStatus;
import com.freetowear.enums.PaymentMethod;
import com.freetowear.enums.PaymentStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentRepositoryTest extends RepositoryTestBase {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnPaymentByOrderId() {
        Order order = persistOrder("customer@mail.com");
        Payment expected = persistPayment(order, PaymentStatus.APPROVED, PaymentMethod.PIX, "125.50");
        flushAndClear();

        var result = paymentRepository.findByOrderId(order.getId());

        assertThat(result).isPresent().get().satisfies(payment -> {
            assertThat(payment.getId()).isEqualTo(expected.getId());
            assertThat(payment.getOrder().getId()).isEqualTo(order.getId());
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(payment.getMethod()).isEqualTo(PaymentMethod.PIX);
            assertThat(payment.getAmountPaid()).isEqualByComparingTo("125.50");
        });
    }

    @Test
    void shouldReturnEmptyWhenOrderHasNoPayment() {
        Order order = persistOrder("customer@mail.com");
        flushAndClear();

        assertThat(paymentRepository.findByOrderId(order.getId())).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenOrderIdDoesNotExist() {
        persistPayment(persistOrder("customer@mail.com"), PaymentStatus.PENDING,
                PaymentMethod.CREDIT_CARD, "80.00");
        flushAndClear();

        assertThat(paymentRepository.findByOrderId("01J00000000000000000000000")).isEmpty();
    }

    @Test
    void shouldNotReturnPaymentBelongingToAnotherOrder() {
        Order requestedOrder = persistOrder("customer@mail.com");
        Order otherOrder = persistOrder("other@mail.com");
        persistPayment(otherOrder, PaymentStatus.APPROVED, PaymentMethod.BOLETO, "200.00");
        flushAndClear();

        assertThat(paymentRepository.findByOrderId(requestedOrder.getId())).isEmpty();
    }

    @Test
    void shouldReturnPaymentRegardlessOfStatusAndMethodValues() {
        Order order = persistOrder("customer@mail.com");
        Payment expected = persistPayment(order, PaymentStatus.REFUNDED, PaymentMethod.CREDIT_CARD, "99.90");
        flushAndClear();

        var result = paymentRepository.findByOrderId(order.getId());

        assertThat(result).isPresent().get().satisfies(payment -> {
            assertThat(payment.getId()).isEqualTo(expected.getId());
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(payment.getMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
            assertThat(payment.getAmountPaid()).isEqualByComparingTo("99.90");
        });
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

    private Payment persistPayment(Order order, PaymentStatus status, PaymentMethod method, String amount) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStatus(status);
        payment.setMethod(method);
        payment.setAmountPaid(new BigDecimal(amount));
        payment.setTransactionCode("TX-" + amount);
        entityManager.persist(payment);
        return payment;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}