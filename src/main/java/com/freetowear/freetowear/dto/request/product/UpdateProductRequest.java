package com.freetowear.freetowear.dto.request.product;

import com.freetowear.freetowear.enums.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Getter
@Setter
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
}