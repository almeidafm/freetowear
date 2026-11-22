package com.freetowear.repository;

import com.freetowear.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    @EntityGraph(attributePaths = "variations")
    Optional<Product> findByName(String name);
    List<Product> findByActiveTrue();
    List<Product> findByCategoryIdAndActiveTrue(String categoryId);
    List<Product> findByNameContainingIgnoreCase(String name);
}