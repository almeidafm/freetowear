package com.freetowear.repository;

import com.freetowear.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    boolean existsByCustomerIdAndProductId(String customerId, String productId);
    Optional<Review> findByCustomerIdAndProductId(String customerId, String productId);
    List<Review> findAllByProductIdOrderByCreatedAtDesc(String productId);
}
