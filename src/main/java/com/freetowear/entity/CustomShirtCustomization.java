package com.freetowear.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "custom_shirt_customization")
public class CustomShirtCustomization extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "custom_shirt_variation_id", nullable = false)
    private CustomShirtVariation variation;

    @Column(nullable = false, length = 500)
    private String imagePublicId;

    @Column(nullable = false, length = 10)
    private String side;

    @Column(nullable = false, precision = 7, scale = 3)
    private BigDecimal axisX;

    @Column(nullable = false, precision = 7, scale = 3)
    private BigDecimal axisY;

    @Column(nullable = false, precision = 7, scale = 3)
    private BigDecimal size;

    @Column(nullable = false, precision = 7, scale = 3)
    private BigDecimal rotation;
}
