package com.freetowear.repository;

import com.freetowear.entity.Coupon;
import com.freetowear.enums.DiscountType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponRepositoryTest extends RepositoryTestBase {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnCouponByCodeIgnoringCase() {
        Coupon expected = persistCoupon("SAVE10", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), true);
        flushAndClear();

        var result = couponRepository.findByCodeIgnoreCase("save10");

        assertThat(result).isPresent().get().satisfies(coupon -> {
            assertThat(coupon.getId()).isEqualTo(expected.getId());
            assertThat(coupon.getCode()).isEqualTo("SAVE10");
            assertThat(coupon.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(coupon.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
            assertThat(coupon.getActive()).isTrue();
        });
    }

    @Test
    void shouldReturnEmptyWhenCouponCodeDoesNotExist() {
        persistCoupon("SAVE10", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), true);
        flushAndClear();

        assertThat(couponRepository.findByCodeIgnoreCase("MISSING")).isEmpty();
    }

    @Test
    void shouldReturnExpiredInactiveCouponByCode() {
        Coupon expected = persistCoupon("EXPIRED", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31), false);
        flushAndClear();

        var result = couponRepository.findByCodeIgnoreCase("expired");

        assertThat(result).isPresent().get().satisfies(coupon -> {
            assertThat(coupon.getId()).isEqualTo(expected.getId());
            assertThat(coupon.getEndDate()).isBefore(LocalDate.of(2026, 1, 1));
            assertThat(coupon.getActive()).isFalse();
        });
    }

    @Test
    void shouldReturnCouponNotYetValidByCode() {
        Coupon expected = persistCoupon("FUTURE", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 12, 31), true);
        flushAndClear();

        var result = couponRepository.findByCodeIgnoreCase("future");

        assertThat(result).isPresent().get().satisfies(coupon -> {
            assertThat(coupon.getId()).isEqualTo(expected.getId());
            assertThat(coupon.getStartDate()).isAfter(LocalDate.of(2026, 1, 1));
            assertThat(coupon.getActive()).isTrue();
        });
    }

    @Test
    void shouldReturnCouponWithInclusiveValidityBoundaryDates() {
        Coupon expected = persistCoupon("BOUNDARY", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 1), true);
        flushAndClear();

        var result = couponRepository.findByCodeIgnoreCase("boundary");

        assertThat(result).isPresent().get().satisfies(coupon -> {
            assertThat(coupon.getId()).isEqualTo(expected.getId());
            assertThat(coupon.getStartDate()).isEqualTo(coupon.getEndDate());
        });
    }

    @Test
    void shouldReturnInactiveCouponByCode() {
        persistCoupon("INACTIVE", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), false);
        flushAndClear();

        assertThat(couponRepository.findByCodeIgnoreCase("inactive"))
                .get().extracting(Coupon::getActive).isEqualTo(false);
    }

    @Test
    void shouldRejectDuplicateCouponCodes() {
        persistCoupon("SAVE10", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), true);
        persistCoupon("save10", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), true);

        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(RuntimeException.class);
    }

    private Coupon persistCoupon(String code, LocalDate startDate, LocalDate endDate, boolean active) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDescription("Test coupon");
        coupon.setDiscountType(DiscountType.PERCENTAGE);
        coupon.setDiscountValue(new BigDecimal("10.00"));
        coupon.setMinimumOrderValue(new BigDecimal("50.00"));
        coupon.setStartDate(startDate);
        coupon.setEndDate(endDate);
        coupon.setActive(active);
        entityManager.persist(coupon);
        return coupon;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}