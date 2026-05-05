package com.freetowear.freetowear.service;

import com.freetowear.freetowear.dto.request.coupon.CreateCouponRequest;
import com.freetowear.freetowear.dto.request.coupon.UpdateCouponRequest;
import com.freetowear.freetowear.entity.Coupon;
import com.freetowear.freetowear.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    public void createCoupon(CreateCouponRequest request) {
        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode());
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinimumOrderValue(request.getMinimumOrderValue());
        coupon.setStartDate(LocalDate.parse(request.getStartDate()));
        coupon.setEndDate(LocalDate.parse(request.getEndDate()));
        coupon.setActive(true);
        couponRepository.save(coupon);
    }

    public void updateCoupon(Integer id, UpdateCouponRequest request) {
        couponRepository.findById(id).ifPresent(coupon -> {
            if (request.getCode() != null) coupon.setCode(request.getCode());
            if (request.getDescription() != null) coupon.setDescription(request.getDescription());
            if (request.getDiscountType() != null) coupon.setDiscountType(request.getDiscountType());
            if (request.getDiscountValue() != null) coupon.setDiscountValue(request.getDiscountValue());
            if (request.getMinimumOrderValue() != null) coupon.setMinimumOrderValue(request.getMinimumOrderValue());
            if (request.getStartDate() != null) coupon.setStartDate(LocalDate.parse(request.getStartDate()));
            if (request.getEndDate() != null) coupon.setEndDate(LocalDate.parse(request.getEndDate()));
            if (request.getActive() != null) coupon.setActive(request.getActive());
            couponRepository.save(coupon);
        });
    }
}