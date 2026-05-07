package com.freetowear.freetowear.dto.request.coupon;

import com.freetowear.freetowear.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateCouponRequest {

    @NotBlank
    private String code;

    private String description;

    private DiscountType discountType;

    @NotNull
    private BigDecimal discountValue;

    private BigDecimal minimumOrderValue;

    @NotBlank
    private String startDate;

    @NotBlank
    private String endDate;

    public CreateCouponRequest() {}

    public CreateCouponRequest(
            String code,
            String description,
            DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minimumOrderValue,
            String startDate,
            String endDate
    ) {
        this.code = code;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minimumOrderValue = minimumOrderValue;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
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
}