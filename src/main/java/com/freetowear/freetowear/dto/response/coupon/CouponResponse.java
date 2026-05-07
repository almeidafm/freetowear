package com.freetowear.freetowear.dto.response.coupon;

import com.freetowear.freetowear.entity.Coupon;
import com.freetowear.freetowear.enums.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CouponResponse {

    private Integer id;
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

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public BigDecimal getMinimumOrderValue() {
        return minimumOrderValue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Boolean getActive() {
        return active;
    }
}