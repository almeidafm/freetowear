package com.freetowear.dto.request.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddItemToOrderRequest {

    @NotNull
    private String idProduct;

    @NotNull
    private String idVariation;

    @NotNull
    @Positive
    private Integer quantity;

    private MultipartFile customerCustomization;
    private String description;

    public AddItemToOrderRequest() {}

    public AddItemToOrderRequest(String idProduct, String idVariation, Integer quantity, MultipartFile customerCustomization, String description) {
        this.idProduct = idProduct;
        this.idVariation = idVariation;
        this.quantity = quantity;
        this.customerCustomization = customerCustomization;
        this.description = description;
    }
}