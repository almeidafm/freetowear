package com.freetowear.freetowear.dto.response.product;

import com.freetowear.freetowear.entity.Product;
import com.freetowear.freetowear.entity.ProductVariation;
import java.math.BigDecimal;
import java.util.List;

public class ProductResponse {

    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String categoryName;
    private Boolean active;
    private List<ProductVariation> variations;

    public ProductResponse() {}

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.imageUrl = product.getImageUrl();
        this.categoryName = product.getCategory().getName();
        this.active = product.getActive();
        this.variations = product.getVariations();
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Boolean getActive() {
        return active;
    }

    public List<ProductVariation> getVariations() {
        return variations;
    }
}
