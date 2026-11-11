package com.freetowear.dto.request.product;

import com.freetowear.enums.Size;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

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
    private String categoryId;

    @NotNull
    private List<MultipartFile> images;

    public CreateProductRequest() {}

    public CreateProductRequest(
            String name,
            String description,
            BigDecimal price,
            String color,
            Size size,
            Integer stock,
            String categoryId,
            MultipartFile image
    ) {
        this(name, description, price, color, size, stock, categoryId, List.of(image));
    }

    public CreateProductRequest(
            String name,
            String description,
            BigDecimal price,
            String color,
            Size size,
            Integer stock,
            String categoryId,
            List<MultipartFile> images
    ) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.color = color;
        this.size = size;
        this.stock = stock;
        this.categoryId = categoryId;
        this.images = images;
    }
}