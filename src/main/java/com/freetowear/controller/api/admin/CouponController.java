package com.freetowear.controller.api.admin;

import com.freetowear.dto.response.coupon.CouponResponse;
import com.freetowear.dto.request.coupon.CreateCouponRequest;
import com.freetowear.dto.request.coupon.UpdateCouponRequest;
import com.freetowear.enums.DiscountType;
import com.freetowear.service.CouponService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping("/create")
    public String createCoupon(
            @RequestParam String code,
            @RequestParam(required = false) String description,
            @RequestParam DiscountType discountType,
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
            @PathVariable String id,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) DiscountType discountType,
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

    @GetMapping
    @ResponseBody
    public List<CouponResponse> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public CouponResponse getCouponById(@PathVariable String id) {
        return couponService.getCouponById(id);
    }
}