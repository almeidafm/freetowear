package com.freetowear.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCustomShirtToCartRequest {
    @NotBlank private String color;
    @NotBlank private String size;
    @NotBlank private String customization;
    private String image;
}
