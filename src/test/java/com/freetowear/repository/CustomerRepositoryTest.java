package com.freetowear.repository;

import com.freetowear.entity.Customer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerRepositoryTest extends RepositoryTestBase {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnTrueWhenEmailExists() {
        persistCustomer("user@mail.com", "11122233344", "11999990000", LocalDateTime.of(2026, 1, 10, 12, 0));
        flushAndClear();

        assertThat(customerRepository.existsByEmail("user@mail.com")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        persistCustomer("user@mail.com", "11122233344", "11999990000", LocalDateTime.of(2026, 1, 10, 12, 0));
        flushAndClear();

        assertThat(customerRepository.existsByEmail("missing@mail.com")).isFalse();
    }

    @Test
    void shouldTreatEmailLookupAsCaseInsensitiveUnderMySqlDefaultCollation() {
        Customer expected = persistCustomer("User@Mail.com", "11122233344", "11999990000", LocalDateTime.now());
        flushAndClear();

        var result = customerRepository.findByEmail("user@mail.com");

        assertThat(result).isPresent().get().extracting(Customer::getId).isEqualTo(expected.getId());
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        persistCustomer("user@mail.com", "11122233344", "11999990000", LocalDateTime.now());
        flushAndClear();

        assertThat(customerRepository.findByEmail("missing@mail.com")).isEmpty();
    }

    @Test
    void shouldFindCustomerByEmailOrPhoneWhenEmailMatches() {
        Customer expected = persistCustomer("user@mail.com", "11122233344", "11999990000", LocalDateTime.now());
        persistCustomer("other@mail.com", "55566677788", "11888880000", LocalDateTime.now());
        flushAndClear();

        var result = customerRepository.findByEmailOrPhone("user@mail.com", "unknown");

        assertThat(result).isPresent().get().extracting(Customer::getId).isEqualTo(expected.getId());
    }

    @Test
    void shouldFindCustomerByEmailOrPhoneWhenPhoneMatches() {
        Customer expected = persistCustomer("user@mail.com", "11122233344", "11999990000", LocalDateTime.now());
        flushAndClear();

        var result = customerRepository.findByEmailOrPhone("unknown@mail.com", "11999990000");

        assertThat(result).isPresent().get().extracting(Customer::getId).isEqualTo(expected.getId());
    }

    @Test
    void shouldReturnEmptyWhenNeitherEmailNorPhoneMatches() {
        persistCustomer("user@mail.com", "11122233344", "11999990000", LocalDateTime.now());
        flushAndClear();

        assertThat(customerRepository.findByEmailOrPhone("missing@mail.com", "11000000000")).isEmpty();
    }

    @Test
    void shouldCountCustomersCreatedBetweenInclusiveBoundaries() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 28, 23, 59);
        persistCustomer("first@mail.com", "11122233344", "11999990001", start);
        persistCustomer("last@mail.com", "55566677788", "11999990002", end);
        persistCustomer("before@mail.com", "99988877766", "11999990003", start.minusSeconds(1));
        persistCustomer("after@mail.com", "44433322211", "11999990004", end.plusSeconds(1));
        flushAndClear();

        assertThat(customerRepository.countByCreatedAtBetween(start, end)).isEqualTo(2);
    }

    @Test
    void shouldReturnZeroWhenNoCustomersWereCreatedInRange() {
        persistCustomer("user@mail.com", "11122233344", "11999990000", LocalDateTime.of(2026, 1, 1, 0, 0));
        flushAndClear();

        assertThat(customerRepository.countByCreatedAtBetween(
                LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 2, 28, 23, 59))).isZero();
    }

    @Test
    void shouldRejectDuplicateEmails() {
        persistCustomer("user@mail.com", "11122233344", "11999990000", LocalDateTime.now());
        persistCustomer("USER@MAIL.COM", "55566677788", "11888880000", LocalDateTime.now());

        assertThatThrownBy(() -> entityManager.flush()).isInstanceOf(RuntimeException.class);
    }

    private Customer persistCustomer(String email, String cpf, String phone, LocalDateTime createdAt) {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail(email);
        customer.setPassword("password");
        customer.setCpf(cpf);
        customer.setPhone(phone);
        customer.setCreatedAt(createdAt);
        customer.setTermsAndConditionsAcceptedAt(createdAt);
        entityManager.persist(customer);
        return customer;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}