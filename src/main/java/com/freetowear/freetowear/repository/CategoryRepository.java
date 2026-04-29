package com.freetowear.freetowear.repository;

import com.freetowear.freetowear.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByName(String nome);

    List<Category> findByNameContainingIgnoreCase(String nome);

    List<Category> findByActiveTrue();

}