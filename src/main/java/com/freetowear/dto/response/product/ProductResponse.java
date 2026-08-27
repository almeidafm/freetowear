package com.freetowear.dto.response.product;

import com.freetowear.entity.Product;
import com.freetowear.entity.ProductVariation;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ProductResponse {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String categoryName;
    private Boolean active;
    private List<VariationResponse> variations;

    public ProductResponse() {}

    public ProductResponse(Product product, String imageUrl) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.imageUrl = imageUrl;
        this.categoryName = product.getCategory().getName();
        this.active = product.getActive();
        this.variations = product.getVariations()
                .stream()
                .map(VariationResponse::new)
                .toList();
    }

    @Getter
    public static class VariationResponse {
        private String id;
        private String color;
        private String size;
        private Integer stock;

        public VariationResponse(ProductVariation variation) {
            this.id = variation.getId();
            this.color = variation.getColor();
            this.size = variation.getSize().name();
            this.stock = variation.getStock();
        }
    }
}