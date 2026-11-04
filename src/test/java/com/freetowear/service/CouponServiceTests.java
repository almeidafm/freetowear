package com.freetowear.service;

import com.freetowear.dto.request.coupon.CreateCouponRequest;
import com.freetowear.dto.request.coupon.UpdateCouponRequest;
import com.freetowear.dto.response.coupon.CouponResponse;
import com.freetowear.entity.Coupon;
import com.freetowear.enums.DiscountType;
import com.freetowear.exception.ResourceNotFoundException;
import com.freetowear.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    private static CouponBuilder aCoupon() {
        return new CouponBuilder();
    }

    private static final class CouponBuilder {
        private final Coupon coupon = new Coupon();

        CouponBuilder withId(String id) { coupon.setId(id); return this; }
        CouponBuilder withCode(String code) { coupon.setCode(code); return this; }
        CouponBuilder withDescription(String description) { coupon.setDescription(description); return this; }
        CouponBuilder withDiscountType(DiscountType type) { coupon.setDiscountType(type); return this; }
        CouponBuilder withDiscountValue(BigDecimal value) { coupon.setDiscountValue(value); return this; }
        CouponBuilder withMinimumOrderValue(BigDecimal value) { coupon.setMinimumOrderValue(value); return this; }
        CouponBuilder withStartDate(LocalDate date) { coupon.setStartDate(date); return this; }
        CouponBuilder withEndDate(LocalDate date) { coupon.setEndDate(date); return this; }
        CouponBuilder withActive(Boolean active) { coupon.setActive(active); return this; }
        Coupon build() { return coupon; }
    }

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @Nested
    @DisplayName("createCoupon")
    class CreateCouponTests {

        @Test
        @DisplayName("Should save all coupon fields and activate coupon")
        void shouldCreateCouponWhenRequestIsValid() {
            CreateCouponRequest request = new CreateCouponRequest("SAVE10", "Ten percent off",
                    DiscountType.PERCENTAGE, new BigDecimal("10.00"), new BigDecimal("50.00"),
                    "2026-01-01", "2026-12-31");

            couponService.createCoupon(request);

            ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
            verify(couponRepository).save(captor.capture());
            Coupon saved = captor.getValue();
            assertThat(saved.getCode()).isEqualTo("SAVE10");
            assertThat(saved.getDescription()).isEqualTo("Ten percent off");
            assertThat(saved.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
            assertThat(saved.getDiscountValue()).isEqualByComparingTo("10.00");
            assertThat(saved.getMinimumOrderValue()).isEqualByComparingTo("50.00");
            assertThat(saved.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(saved.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
            assertThat(saved.getActive()).isTrue();
        }

        @Test
        @DisplayName("Should propagate date parsing failure and not save coupon")
        void shouldNotCreateCouponWhenDateIsInvalid() {
            CreateCouponRequest request = new CreateCouponRequest("SAVE10", null,
                    DiscountType.FIXED, BigDecimal.TEN, null, "invalid", "2026-12-31");

            assertThatThrownBy(() -> couponService.createCoupon(request))
                    .isInstanceOf(java.time.format.DateTimeParseException.class);
            verify(couponRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateCoupon")
    class UpdateCouponTests {

        @Test
        @DisplayName("Should update every non-null coupon field")
        void shouldUpdateCouponWhenAllFieldsAreProvided() {
            Coupon coupon = aCoupon().withCode("OLD").withDescription("Old description")
                    .withDiscountType(DiscountType.FIXED).withDiscountValue(new BigDecimal("5.00"))
                    .withMinimumOrderValue(new BigDecimal("20.00")).withStartDate(LocalDate.of(2026, 1, 1))
                    .withEndDate(LocalDate.of(2026, 6, 30)).withActive(true).build();
            when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
            UpdateCouponRequest request = new UpdateCouponRequest("NEW", "New description",
                    DiscountType.PERCENTAGE, new BigDecimal("15.00"), new BigDecimal("30.00"),
                    "2026-02-01", "2026-12-31", false);

            couponService.updateCoupon("coupon-1", request);

            assertThat(coupon.getCode()).isEqualTo("NEW");
            assertThat(coupon.getDescription()).isEqualTo("New description");
            assertThat(coupon.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
            assertThat(coupon.getDiscountValue()).isEqualByComparingTo("15.00");
            assertThat(coupon.getMinimumOrderValue()).isEqualByComparingTo("30.00");
            assertThat(coupon.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 1));
            assertThat(coupon.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
            assertThat(coupon.getActive()).isFalse();
            verify(couponRepository).save(coupon);
        }

        @Test
        @DisplayName("Should preserve fields when update values are null")
        void shouldPreserveFieldsWhenUpdateValuesAreNull() {
            Coupon coupon = aCoupon().withCode("SAVE10").withDescription("Description")
                    .withDiscountType(DiscountType.PERCENTAGE).withDiscountValue(new BigDecimal("10.00"))
                    .withMinimumOrderValue(new BigDecimal("50.00")).withStartDate(LocalDate.of(2026, 1, 1))
                    .withEndDate(LocalDate.of(2026, 12, 31)).withActive(true).build();
            when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));

            couponService.updateCoupon("coupon-1", new UpdateCouponRequest());

            assertThat(coupon.getCode()).isEqualTo("SAVE10");
            assertThat(coupon.getDescription()).isEqualTo("Description");
            assertThat(coupon.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
            assertThat(coupon.getDiscountValue()).isEqualByComparingTo("10.00");
            assertThat(coupon.getMinimumOrderValue()).isEqualByComparingTo("50.00");
            assertThat(coupon.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(coupon.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
            assertThat(coupon.getActive()).isTrue();
            verify(couponRepository).save(coupon);
        }

        @Test
        @DisplayName("Should not save when coupon does not exist")
        void shouldNotUpdateCouponWhenCouponDoesNotExist() {
            when(couponRepository.findById("missing")).thenReturn(Optional.empty());

            couponService.updateCoupon("missing", new UpdateCouponRequest("NEW", null, null, null, null, null, null, null));

            verify(couponRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getCouponById")
    class GetCouponByIdTests {

        @Test
        @DisplayName("Should return mapped coupon when coupon exists")
        void shouldReturnCouponWhenCouponExists() {
            Coupon coupon = aCoupon().withId("coupon-1").withCode("SAVE10")
                    .withDescription("Description").withDiscountType(DiscountType.PERCENTAGE)
                    .withDiscountValue(new BigDecimal("10.00")).withMinimumOrderValue(new BigDecimal("50.00"))
                    .withStartDate(LocalDate.of(2026, 1, 1)).withEndDate(LocalDate.of(2026, 12, 31))
                    .withActive(true).build();
            when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));

            CouponResponse response = couponService.getCouponById("coupon-1");

            assertThat(response.getId()).isEqualTo("coupon-1");
            assertThat(response.getCode()).isEqualTo("SAVE10");
            assertThat(response.getDescription()).isEqualTo("Description");
            assertThat(response.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
            assertThat(response.getDiscountValue()).isEqualByComparingTo("10.00");
            assertThat(response.getMinimumOrderValue()).isEqualByComparingTo("50.00");
            assertThat(response.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(response.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
            assertThat(response.getActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when coupon does not exist")
        void shouldThrowExceptionWhenCouponDoesNotExist() {
            when(couponRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.getCouponById("missing"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Coupon not found: missing");
        }
    }

    @Nested
    @DisplayName("getCouponByCode")
    class GetCouponByCodeTests {

        @Test
        @DisplayName("Should return coupon found by case-insensitive code")
        void shouldReturnCouponWhenCodeExists() {
            Coupon coupon = aCoupon().withId("coupon-1").withCode("SAVE10").withActive(true).build();
            when(couponRepository.findByCodeIgnoreCase("save10")).thenReturn(Optional.of(coupon));

            CouponResponse response = couponService.getCouponByCode("save10");

            assertThat(response.getId()).isEqualTo("coupon-1");
            assertThat(response.getCode()).isEqualTo("SAVE10");
            verify(couponRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should fall back to ID when code lookup is empty")
        void shouldReturnCouponByIdWhenCodeLookupFails() {
            Coupon coupon = aCoupon().withId("coupon-1").withCode("SAVE10").build();
            when(couponRepository.findByCodeIgnoreCase("coupon-1")).thenReturn(Optional.empty());
            when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));

            CouponResponse response = couponService.getCouponByCode("coupon-1");

            assertThat(response.getId()).isEqualTo("coupon-1");
            verify(couponRepository).findByCodeIgnoreCase("coupon-1");
            verify(couponRepository).findById("coupon-1");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when code and ID are not found")
        void shouldThrowExceptionWhenCodeAndIdDoNotExist() {
            when(couponRepository.findByCodeIgnoreCase("missing")).thenReturn(Optional.empty());
            when(couponRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.getCouponByCode("missing"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Coupon not found: missing");
        }
    }

    @Nested
    @DisplayName("getAllCoupons")
    class GetAllCouponsTests {

        @Test
        @DisplayName("Should map every coupon response field")
        void shouldReturnAllCouponsWhenCouponsExist() {
            Coupon coupon = aCoupon().withId("coupon-1").withCode("SAVE10")
                    .withDescription("Description").withDiscountType(DiscountType.PERCENTAGE)
                    .withDiscountValue(new BigDecimal("10.00")).withMinimumOrderValue(new BigDecimal("50.00"))
                    .withStartDate(LocalDate.of(2026, 1, 1)).withEndDate(LocalDate.of(2026, 12, 31))
                    .withActive(true).build();
            when(couponRepository.findAll()).thenReturn(List.of(coupon));

            List<CouponResponse> responses = couponService.getAllCoupons();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getId()).isEqualTo("coupon-1");
            assertThat(responses.get(0).getCode()).isEqualTo("SAVE10");
            assertThat(responses.get(0).getDescription()).isEqualTo("Description");
            assertThat(responses.get(0).getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
            assertThat(responses.get(0).getDiscountValue()).isEqualByComparingTo("10.00");
            assertThat(responses.get(0).getMinimumOrderValue()).isEqualByComparingTo("50.00");
            assertThat(responses.get(0).getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(responses.get(0).getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
            assertThat(responses.get(0).getActive()).isTrue();
        }

        @Test
        @DisplayName("Should return empty list when no coupons exist")
        void shouldReturnEmptyListWhenNoCouponsExist() {
            when(couponRepository.findAll()).thenReturn(List.of());

            assertThat(couponService.getAllCoupons()).isEmpty();
        }
    }
}