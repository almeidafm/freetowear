package com.freetowear.freetowear.dto.request.coupon;

import com.freetowear.freetowear.enums.DiscountType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateCouponRequest {

    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderValue;
    private String startDate;
    private String endDate;
    private Boolean active;

    public UpdateCouponRequest() {}

    public UpdateCouponRequest(
            String code,
            String description,
            DiscountType discountType,
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
}