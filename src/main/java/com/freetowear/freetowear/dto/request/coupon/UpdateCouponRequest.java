package com.freetowear.freetowear.dto.request.coupon;

import com.freetowear.freetowear.entity.Coupon;
import java.math.BigDecimal;

public class UpdateCouponRequest {

    private String code;
    private String description;
    private Coupon.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderValue;
    private String startDate;
    private String endDate;
    private Boolean active;

    public UpdateCouponRequest() {}

    public UpdateCouponRequest(
            String code,
            String description,
            Coupon.DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minimumOrderValue,
            String startDate,
            String endDate,
            Boolean active
    ) {
        this.code = code;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minimumOrderValue = minimumOrderValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Coupon.DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(Coupon.DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMinimumOrderValue() {
        return minimumOrderValue;
    }

    public void setMinimumOrderValue(BigDecimal minimumOrderValue) {
        this.minimumOrderValue = minimumOrderValue;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}