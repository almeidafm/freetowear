package com.freetowear.freetowear.entity;

import com.freetowear.freetowear.util.UlidEntityListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(UlidEntityListener.class)
public abstract class BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 26)
    private String id;
}