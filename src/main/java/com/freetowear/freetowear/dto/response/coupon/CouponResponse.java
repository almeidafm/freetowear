package com.freetowear.freetowear.dto.response.coupon;

import com.freetowear.freetowear.entity.Coupon;
import com.freetowear.freetowear.enums.DiscountType;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class CouponResponse {

    private String id;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;

    public CouponResponse() {}

    public CouponResponse(Coupon coupon) {
        this.id = coupon.getId();
        this.code = coupon.getCode();
        this.description = coupon.getDescription();
        this.discountType = coupon.getDiscountType();
        this.discountValue = coupon.getDiscountValue();
        this.minimumOrderValue = coupon.getMinimumOrderValue();
        this.startDate = coupon.getStartDate();
        this.endDate = coupon.getEndDate();
        this.active = coupon.getActive();
    }
}