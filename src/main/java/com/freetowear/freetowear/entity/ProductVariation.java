package com.freetowear.freetowear.entity;

import com.freetowear.freetowear.enums.Size;
import jakarta.persistence.*;
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
}