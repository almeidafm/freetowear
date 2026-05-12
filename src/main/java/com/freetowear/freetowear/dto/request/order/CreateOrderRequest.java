package com.freetowear.freetowear.dto.request.order;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull
    private Integer idCustomer;

    @NotNull
    private Integer idAddress;

    private Integer idCoupon;

    public CreateOrderRequest() {}

    public CreateOrderRequest(Integer idCustomer, Integer idAddress, Integer idCoupon) {
        this.idCustomer = idCustomer;
        this.idAddress = idAddress;
        this.idCoupon = idCoupon;
    }
}