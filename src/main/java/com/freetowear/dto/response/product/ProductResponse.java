package com.freetowear.dto.response.product;

import com.freetowear.entity.Product;
import com.freetowear.entity.ProductVariation;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

@Getter
public class ProductResponse {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal ratingAverage;
    private Integer ratingCount;
    private String imageUrl;
    private String categoryName;
    private Boolean active;
    private List<VariationResponse> variations;

    public ProductResponse() {}

    public ProductResponse(Product product, String imageUrl) {
        this(product, imageUrl, Function.identity());
    }

    public ProductResponse(Product product, String imageUrl, Function<String, String> imageUrlBuilder) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.ratingAverage = product.getRatingAverage();
        this.ratingCount = product.getRatingCount();
        this.imageUrl = imageUrl;
        this.categoryName = product.getCategory().getName();
        this.active = product.getActive();
        this.variations = product.getVariations()
                .stream()
                .map(variation -> new VariationResponse(variation, imageUrlBuilder))
                .toList();
    }

    @Getter
    public static class VariationResponse {
        private String id;
        private String color;
        private String size;
        private Integer stock;
        private List<String> imageUrls;

        public VariationResponse(ProductVariation variation, Function<String, String> imageUrlBuilder) {
            this.id = variation.getId();
            this.color = variation.getColor();
            this.size = variation.getSize().name();
            this.stock = variation.getStock();
            this.imageUrls = variation.getImages() == null ? List.of() : variation.getImages().stream()
                    .sorted((left, right) -> Integer.compare(left.getDisplayOrder(), right.getDisplayOrder()))
                    .map(image -> imageUrlBuilder.apply(image.getImagePublicId()))
                    .toList();
        }
    }
}