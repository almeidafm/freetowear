package com.freetowear.freetowear.dto.request.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}