package com.freetowear.dto.request.order;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull
    private String idCustomer;

    @NotNull
    private String idAddress;

    private String idCoupon;

    public CreateOrderRequest() {}

    public CreateOrderRequest(String idCustomer, String idAddress, String idCoupon) {
        this.idCustomer = idCustomer;
        this.idAddress = idAddress;
        this.idCoupon = idCoupon;
    }
}