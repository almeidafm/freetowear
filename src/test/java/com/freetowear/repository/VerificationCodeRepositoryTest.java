package com.freetowear.repository;

import com.freetowear.entity.Customer;
import com.freetowear.entity.VerificationCode;
import com.freetowear.enums.VerificationType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationCodeRepositoryTest extends RepositoryTestBase {

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnLatestActiveCodeForCustomerAndType() {
        Customer customer = persistCustomer("customer@mail.com");
        VerificationCode older = persistCode(customer, VerificationType.EMAIL, "111111",
                LocalDateTime.of(2026, 1, 1, 12, 0), LocalDateTime.of(2026, 2, 1, 12, 0));
        VerificationCode latest = persistCode(customer, VerificationType.EMAIL, "222222",
                LocalDateTime.of(2026, 1, 2, 12, 0), LocalDateTime.of(2026, 2, 2, 12, 0));
        flushAndClear();

        var result = verificationCodeRepository
                .findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseOrderByCreatedAtDesc(
                        customer, VerificationType.EMAIL);

        assertThat(result).isPresent().get().satisfies(code -> {
            assertThat(code.getId()).isEqualTo(latest.getId());
            assertThat(code.getId()).isNotEqualTo(older.getId());
            assertThat(code.getCode()).isEqualTo("222222");
        });
    }

    @Test
    void shouldExcludeUsedInvalidatedAndOtherTypeCodesFromLatestActiveLookup() {
        Customer customer = persistCustomer("customer@mail.com");
        VerificationCode expected = persistCode(customer, VerificationType.EMAIL, "111111",
                LocalDateTime.of(2026, 1, 1, 12, 0), LocalDateTime.of(2026, 2, 1, 12, 0));
        VerificationCode used = persistCode(customer, VerificationType.EMAIL, "222222",
                LocalDateTime.of(2026, 1, 2, 12, 0), LocalDateTime.of(2026, 2, 2, 12, 0));
        used.setUsed(true);
        VerificationCode invalidated = persistCode(customer, VerificationType.EMAIL, "333333",
                LocalDateTime.of(2026, 1, 3, 12, 0), LocalDateTime.of(2026, 2, 3, 12, 0));
        invalidated.setInvalidated(true);
        persistCode(customer, VerificationType.PASSWORD_RESET, "444444",
                LocalDateTime.of(2026, 1, 4, 12, 0), LocalDateTime.of(2026, 2, 4, 12, 0));
        flushAndClear();

        assertThat(verificationCodeRepository
                .findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseOrderByCreatedAtDesc(
                        customer, VerificationType.EMAIL))
                .get().extracting(VerificationCode::getId).isEqualTo(expected.getId());
    }

    @Test
    void shouldReturnEmptyWhenCustomerHasNoActiveCode() {
        Customer customer = persistCustomer("customer@mail.com");
        VerificationCode code = persistCode(customer, VerificationType.EMAIL, "111111",
                LocalDateTime.of(2026, 1, 1, 12, 0), LocalDateTime.of(2026, 2, 1, 12, 0));
        code.setUsed(true);
        flushAndClear();

        assertThat(verificationCodeRepository
                .findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseOrderByCreatedAtDesc(
                        customer, VerificationType.EMAIL)).isEmpty();
    }

    @Test
    void shouldReturnAllActiveCodesForCustomerAndType() {
        Customer customer = persistCustomer("customer@mail.com");
        VerificationCode first = persistCode(customer, VerificationType.EMAIL, "111111",
                LocalDateTime.of(2026, 1, 1, 12, 0), LocalDateTime.of(2026, 2, 1, 12, 0));
        VerificationCode second = persistCode(customer, VerificationType.EMAIL, "222222",
                LocalDateTime.of(2026, 1, 2, 12, 0), LocalDateTime.of(2026, 2, 2, 12, 0));
        persistCode(persistCustomer("other@mail.com"), VerificationType.EMAIL, "333333",
                LocalDateTime.of(2026, 1, 3, 12, 0), LocalDateTime.of(2026, 2, 3, 12, 0));
        persistCode(customer, VerificationType.PASSWORD_RESET, "444444",
                LocalDateTime.of(2026, 1, 4, 12, 0), LocalDateTime.of(2026, 2, 4, 12, 0));
        flushAndClear();

        assertThat(verificationCodeRepository.findByCustomerAndTypeAndUsedFalseAndInvalidatedFalse(
                customer, VerificationType.EMAIL)).extracting(VerificationCode::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void shouldReturnAllActiveCodesWithIdenticalCreatedTimestamps() {
        Customer customer = persistCustomer("customer@mail.com");
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 12, 0);
        VerificationCode first = persistCode(customer, VerificationType.EMAIL, "111111", createdAt,
                LocalDateTime.of(2026, 2, 1, 12, 0));
        VerificationCode second = persistCode(customer, VerificationType.EMAIL, "222222", createdAt,
                LocalDateTime.of(2026, 2, 1, 12, 0));
        flushAndClear();

        assertThat(verificationCodeRepository.findByCustomerAndTypeAndUsedFalseAndInvalidatedFalse(
                customer, VerificationType.EMAIL)).extracting(VerificationCode::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void shouldReturnEmptyWhenCustomerHasNoActiveCodesOfType() {
        Customer customer = persistCustomer("customer@mail.com");
        persistCode(customer, VerificationType.PASSWORD_RESET, "111111",
                LocalDateTime.of(2026, 1, 1, 12, 0), LocalDateTime.of(2026, 2, 1, 12, 0));
        flushAndClear();

        assertThat(verificationCodeRepository.findByCustomerAndTypeAndUsedFalseAndInvalidatedFalse(
                customer, VerificationType.EMAIL)).isEmpty();
    }

    @Test
    void shouldReturnLatestCodeThatExpiresAfterReferenceTime() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 1, 12, 0);
        Customer customer = persistCustomer("customer@mail.com");
        VerificationCode expired = persistCode(customer, VerificationType.EMAIL, "111111",
                LocalDateTime.of(2026, 1, 1, 12, 0), now);
        VerificationCode valid = persistCode(customer, VerificationType.EMAIL, "222222",
                LocalDateTime.of(2026, 1, 2, 12, 0), now.plusSeconds(1));
        flushAndClear();

        assertThat(verificationCodeRepository
                .findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        customer, VerificationType.EMAIL, now))
                .get().satisfies(code -> {
                    assertThat(code.getId()).isEqualTo(valid.getId());
                    assertThat(code.getId()).isNotEqualTo(expired.getId());
                });
    }

    @Test
    void shouldReturnEmptyWhenCodeExpiresAtReferenceTime() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 1, 12, 0);
        Customer customer = persistCustomer("customer@mail.com");
        persistCode(customer, VerificationType.EMAIL, "111111",
                LocalDateTime.of(2026, 1, 1, 12, 0), now);
        flushAndClear();

        assertThat(verificationCodeRepository
                .findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        customer, VerificationType.EMAIL, now)).isEmpty();
    }

    @Test
    void shouldDeleteOnlyCodesExpiredBeforeReferenceTime() {
        LocalDateTime reference = LocalDateTime.of(2026, 2, 1, 12, 0);
        Customer customer = persistCustomer("customer@mail.com");
        VerificationCode expired = persistCode(customer, VerificationType.EMAIL, "111111",
                LocalDateTime.of(2026, 1, 1, 12, 0), reference.minusSeconds(1));
        VerificationCode atBoundary = persistCode(customer, VerificationType.EMAIL, "222222",
                LocalDateTime.of(2026, 1, 2, 12, 0), reference);
        VerificationCode valid = persistCode(persistCustomer("other@mail.com"), VerificationType.PHONE, "333333",
                LocalDateTime.of(2026, 1, 3, 12, 0), reference.plusSeconds(1));
        flushAndClear();

        int deleted = verificationCodeRepository.deleteByExpiresAtBefore(reference);
        entityManager.flush();
        entityManager.clear();

        assertThat(deleted).isEqualTo(1);
        assertThat(entityManager.find(VerificationCode.class, expired.getId())).isNull();
        assertThat(entityManager.find(VerificationCode.class, atBoundary.getId())).isNotNull();
        assertThat(entityManager.find(VerificationCode.class, valid.getId())).isNotNull();
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

    private VerificationCode persistCode(Customer customer, VerificationType type, String value,
                                         LocalDateTime createdAt, LocalDateTime expiresAt) {
        VerificationCode code = new VerificationCode();
        code.setCustomer(customer);
        code.setType(type);
        code.setCode(value);
        code.setCreatedAt(createdAt);
        code.setExpiresAt(expiresAt);
        entityManager.persist(code);
        return code;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}