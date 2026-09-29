package com.freetowear.repository;

import com.freetowear.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, String> {
    List<ReviewImage> findAllByReviewIdOrderByDisplayOrderAsc(String reviewId);
}
