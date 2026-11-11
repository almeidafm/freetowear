package com.freetowear.repository;

import com.freetowear.entity.ProductVariationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariationImageRepository extends JpaRepository<ProductVariationImage, String> {

    List<ProductVariationImage> findAllByProductVariationIdOrderByDisplayOrderAsc(String productVariationId);
}