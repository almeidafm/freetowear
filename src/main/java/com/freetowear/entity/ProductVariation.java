package com.freetowear.entity;

import com.freetowear.enums.Size;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_variation",
        indexes = {
                @Index(name = "idx_color", columnList = "color"),
                @Index(name = "idx_size", columnList = "size")
        })
public class ProductVariation extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, length = 50)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size;

    @Column(nullable = false)
    private Integer stock = 0;

    @OneToMany(mappedBy = "productVariation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariationImage> images = new ArrayList<>();
}