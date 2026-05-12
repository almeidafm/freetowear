package com.freetowear.freetowear.dto.request.product;

import com.freetowear.freetowear.enums.Size;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotBlank
    private String color;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Size size;

    @NotNull
    @Positive
    private Integer stock;

    @NotNull
    private Integer categoryId;

    @NotNull
    private MultipartFile image;

    public CreateProductRequest() {}

    public CreateProductRequest(
            String name,
            String description,
            BigDecimal price,
            String color,
            Size size,
            Integer stock,
            Integer categoryId,
            MultipartFile image
    ) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.color = color;
        this.size = size;
        this.stock = stock;
        this.categoryId = categoryId;
        this.image = image;
    }
}