package com.freetowear.freetowear.controller.api.admin;

import com.freetowear.freetowear.entity.Coupon;
import com.freetowear.freetowear.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * CouponController — manages discount coupons.
 * POST   /coupon/create ✔
 * GET    /coupon ⏳
 * GET    /coupon/{id} ⏳
 * PATCH  /coupon/{id} ⏳ - must complete all fields to work, I shall change that later.
 * */
@Controller
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponRepository couponRepository;

    @PostMapping("/create")
    public String createCoupon(
            @RequestParam String codigo,
            @RequestParam(required = false) String descricao,
            @RequestParam Coupon.DiscountType tipoDesconto,
            @RequestParam BigDecimal valorDesconto,
            @RequestParam(required = false) BigDecimal valorMinimoPedido,
            @RequestParam String dataInicio,
            @RequestParam String dataFim
    ) {

        Coupon coupon = new Coupon();
        coupon.setCode(codigo);
        coupon.setDescription(descricao);
        coupon.setDiscountType(tipoDesconto);
        coupon.setDiscountValue(valorDesconto);
        coupon.setMinimumOrderValue(valorMinimoPedido);
        coupon.setStartDate(LocalDate.parse(dataInicio));
        coupon.setEndDate(LocalDate.parse(dataFim));
        coupon.setActive(true);

        couponRepository.save(coupon);

        return "redirect:/";
    }

    @PatchMapping("/{id}")
    public String updateCoupon(
            @PathVariable Integer id,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Coupon.DiscountType discountType,
            @RequestParam(required = false) BigDecimal discountValue,
            @RequestParam(required = false) BigDecimal minimunOrderValue,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Boolean active
    ) {
        couponRepository.findById(id).ifPresent(coupon -> {
            if (code != null) coupon.setCode(code);
            if (description != null) coupon.setDescription(description);
            if (discountType != null) coupon.setDiscountType(discountType);
            if (discountValue != null) coupon.setDiscountValue(discountValue);
            if (minimunOrderValue != null) coupon.setMinimumOrderValue(minimunOrderValue);
            if (startDate != null) coupon.setStartDate(LocalDate.parse(startDate));
            if (endDate != null) coupon.setEndDate(LocalDate.parse(endDate));
            if (active != null) coupon.setActive(active);

            couponRepository.save(coupon);
        });

        return "redirect:/";
    }
}