package com.freetowear.freetowear.dto.request.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AddItemToOrderRequest {

    @NotNull
    private Integer idProduct;

    @NotNull
    private Integer idVariation;

    @NotNull
    @Positive
    private Integer quantity;

    public AddItemToOrderRequest() {}

    public AddItemToOrderRequest(Integer idProduct, Integer idVariation, Integer quantity) {
        this.idProduct = idProduct;
        this.idVariation = idVariation;
        this.quantity = quantity;
    }

    public Integer getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Integer idProduct) {
        this.idProduct = idProduct;
    }

    public Integer getIdVariation() {
        return idVariation;
    }

    public void setIdVariation(Integer idVariation) {
        this.idVariation = idVariation;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}