package com.freetowear.freetowear.dto.request.product;

import com.freetowear.freetowear.enums.Size;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

public class UpdateProductRequest {

    private String name;
    private String description;
    private BigDecimal price;
    private String color;
    private Size size;
    private Integer stock;
    private Integer categoryId;
    private MultipartFile image;
    private Boolean active;

    public UpdateProductRequest(String name, String description, BigDecimal price,
                                String color, Size size, Integer stock,
                                Integer categoryId, MultipartFile image, Boolean active) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.color = color;
        this.size = size;
        this.stock = stock;
        this.categoryId = categoryId;
        this.image = image;
        this.active = active;
    }

    // getters...
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}