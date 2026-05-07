package com.freetowear.freetowear.dto.request.product;

import com.freetowear.freetowear.enums.Size;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }
}