package com.freetowear.service;

import com.freetowear.entity.Coupon;
import com.freetowear.exception.ResourceNotFoundException;
import com.freetowear.repository.CouponRepository;
import com.freetowear.dto.request.coupon.CreateCouponRequest;
import com.freetowear.dto.request.coupon.UpdateCouponRequest;
import com.freetowear.dto.response.coupon.CouponResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    public void createCoupon(CreateCouponRequest request) {
        if (request == null) throw new IllegalArgumentException("Coupon request is required");
        String code = normalizeCode(request.getCode());
        LocalDate start = parseDate(request.getStartDate());
        LocalDate end = parseDate(request.getEndDate());
        validateCoupon(code, request.getDiscountType(), request.getDiscountValue(),
                request.getMinimumOrderValue(), start, end);
        ensureCodeAvailable(code, null);
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinimumOrderValue(request.getMinimumOrderValue());
        coupon.setStartDate(start);
        coupon.setEndDate(end);
        coupon.setActive(true);
        couponRepository.save(coupon);
    }

    public void updateCoupon(String id, UpdateCouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + id));
        if (request == null) throw new IllegalArgumentException("Coupon request is required");
        String code = request.getCode() == null ? coupon.getCode() : normalizeCode(request.getCode());
        DiscountTypeHolder values = new DiscountTypeHolder(
                request.getDiscountType() == null ? coupon.getDiscountType() : request.getDiscountType(),
                request.getDiscountValue() == null ? coupon.getDiscountValue() : request.getDiscountValue(),
                request.getMinimumOrderValue() == null ? coupon.getMinimumOrderValue() : request.getMinimumOrderValue(),
                request.getStartDate() == null ? coupon.getStartDate() : parseDate(request.getStartDate()),
                request.getEndDate() == null ? coupon.getEndDate() : parseDate(request.getEndDate()));
        validateCoupon(code, values.type, values.value, values.minimum, values.start, values.end);
        ensureCodeAvailable(code, id);
        coupon.setCode(code);
        if (request.getDescription() != null) coupon.setDescription(request.getDescription());
        coupon.setDiscountType(values.type);
        coupon.setDiscountValue(values.value);
        coupon.setMinimumOrderValue(values.minimum);
        coupon.setStartDate(values.start);
        coupon.setEndDate(values.end);
        if (request.getActive() != null) coupon.setActive(request.getActive());
        couponRepository.save(coupon);
    }

    public CouponResponse getCouponById(String id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + id));
        return new CouponResponse(coupon);
    }

    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .or(() -> couponRepository.findById(code))
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));
        return new CouponResponse(coupon);
    }


    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll()
                .stream()
                .map(CouponResponse::new)
                .collect(Collectors.toList());
    }

    private String normalizeCode(String code) {
        if (code == null || code.trim().isEmpty()) throw new IllegalArgumentException("Coupon code is required");
        String normalized = code.trim().toUpperCase();
        if (normalized.length() > 20) throw new IllegalArgumentException("Coupon code cannot exceed 20 characters");
        return normalized;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Coupon dates are required");
        try { return LocalDate.parse(value); }
        catch (java.time.format.DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid coupon date", exception);
        }
    }

    private void validateCoupon(String code, com.freetowear.enums.DiscountType type, BigDecimal value,
                                BigDecimal minimum, LocalDate start, LocalDate end) {
        if (type == null || value == null || value.signum() < 0) throw new IllegalArgumentException("Invalid discount");
        if (type == com.freetowear.enums.DiscountType.PERCENTAGE && value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100");
        }
        if (minimum != null && minimum.signum() < 0) throw new IllegalArgumentException("Minimum order value cannot be negative");
        if (end.isBefore(start)) throw new IllegalArgumentException("Coupon end date cannot be before start date");
    }

    private void ensureCodeAvailable(String code, String currentId) {
        couponRepository.findByCodeIgnoreCase(code).ifPresent(existing -> {
            if (currentId == null || !currentId.equals(existing.getId())) {
                throw new IllegalArgumentException("Coupon code already in use");
            }
        });
    }

    private static final class DiscountTypeHolder {
        private final com.freetowear.enums.DiscountType type;
        private final BigDecimal value;
        private final BigDecimal minimum;
        private final LocalDate start;
        private final LocalDate end;
        private DiscountTypeHolder(com.freetowear.enums.DiscountType type, BigDecimal value, BigDecimal minimum,
                                   LocalDate start, LocalDate end) {
            this.type = type; this.value = value; this.minimum = minimum; this.start = start; this.end = end;
        }
    }
}