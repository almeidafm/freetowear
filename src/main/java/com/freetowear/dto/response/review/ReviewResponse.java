package com.freetowear.dto.response.review;

import com.freetowear.entity.Review;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ReviewResponse {

    private String customerName;
    private Integer rating;
    private String text;
    private LocalDateTime createdAt;
    private List<String> imageUrls;

    public ReviewResponse() {}

    public ReviewResponse(Review review, List<String> imageUrls) {
        this.customerName = review.getCustomer().getName();
        this.rating = review.getRating();
        this.text = review.getText();
        this.createdAt = review.getCreatedAt();
        this.imageUrls = imageUrls;
    }
}
