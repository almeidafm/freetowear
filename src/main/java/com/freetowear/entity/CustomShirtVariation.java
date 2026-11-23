package com.freetowear.entity;

import com.freetowear.enums.Color;
import com.freetowear.enums.Size;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "custom_shirt_variation", indexes = {
        @Index(name = "idx_custom_shirt_variation_color", columnList = "color"),
        @Index(name = "idx_custom_shirt_variation_size", columnList = "size")
})
public class CustomShirtVariation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "custom_shirt_id", nullable = false)
    private CustomShirt customShirt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Color color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size;
}
