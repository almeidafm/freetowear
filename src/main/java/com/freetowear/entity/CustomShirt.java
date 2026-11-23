package com.freetowear.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "custom_shirt", indexes = {
        @Index(name = "idx_custom_shirt_name", columnList = "name"),
        @Index(name = "idx_custom_shirt_category", columnList = "category_id")
})
public class CustomShirt extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAverage = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer ratingCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "customShirt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomShirtVariation> variations = new ArrayList<>();
}
