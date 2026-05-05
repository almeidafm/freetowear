package com.freetowear.freetowear.dto.request.order;

import jakarta.validation.constraints.NotNull;

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

    public Integer getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(Integer idCustomer) {
        this.idCustomer = idCustomer;
    }

    public Integer getIdAddress() {
        return idAddress;
    }

    public void setIdAddress(Integer idAddress) {
        this.idAddress = idAddress;
    }

    public Integer getIdCoupon() {
        return idCoupon;
    }

    public void setIdCoupon(Integer idCoupon) {
        this.idCoupon = idCoupon;
    }
}