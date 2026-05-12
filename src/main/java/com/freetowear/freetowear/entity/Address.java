package com.freetowear.freetowear.entity;

import com.freetowear.freetowear.enums.UF;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "address", indexes = {
        @Index(name = "idx_cep", columnList = "cep")
})
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "cep", nullable = false, length = 8)
    private String cep;

    @Column(nullable = false, length = 150)
    private String street;

    @Column(length = 10)
    private String number;

    @Column(length = 100)
    private String complement;

    @Column(nullable = false, length = 100)
    private String neighborhood;

    @Column(nullable = false, length = 100)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private UF state;

    @Column(nullable = false)
    private Boolean defaultAddress = false;
}