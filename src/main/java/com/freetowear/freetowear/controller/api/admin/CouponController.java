package com.freetowear.freetowear.controller.api.admin;

import com.freetowear.freetowear.dto.request.coupon.CreateCouponRequest;
import com.freetowear.freetowear.dto.request.coupon.UpdateCouponRequest;
import com.freetowear.freetowear.entity.Coupon;
import com.freetowear.freetowear.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

/*
 * CouponController — manages discount coupons.
 * POST   /coupon/create ✔
 * GET    /coupon ⏳
 * GET    /coupon/{id} ⏳
 * PATCH  /coupon/{id} ✔
 * */
@Controller
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping("/create")
    public String createCoupon(
            @RequestParam String code,
            @RequestParam(required = false) String description,
            @RequestParam Coupon.DiscountType discountType,
            @RequestParam BigDecimal discountValue,
            @RequestParam(required = false) BigDecimal minimumOrderValue,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        couponService.createCoupon(new CreateCouponRequest(
                code, description, discountType, discountValue,
                minimumOrderValue, startDate, endDate
        ));
        return "redirect:/";
    }

    @PatchMapping("/{id}")
    public String updateCoupon(
            @PathVariable Integer id,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Coupon.DiscountType discountType,
            @RequestParam(required = false) BigDecimal discountValue,
            @RequestParam(required = false) BigDecimal minimumOrderValue,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Boolean active
    ) {
        couponService.updateCoupon(id, new UpdateCouponRequest(
                code, description, discountType, discountValue,
                minimumOrderValue, startDate, endDate, active
        ));
        return "redirect:/";
    }
}