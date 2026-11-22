package com.freetowear.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_variation_image")
public class ProductVariationImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variation_id", nullable = false)
    private ProductVariation productVariation;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String imagePublicId;

    @Column(nullable = false)
    private Integer displayOrder = 0;
}