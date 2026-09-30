package com.freetowear.controller.api.customer;

import com.freetowear.dto.request.review.CreateReviewRequest;
import com.freetowear.entity.Review;
import com.freetowear.infra.security.CustomerDetails;
import com.freetowear.service.ReviewService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> createReview(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        try {
            Review review = reviewService.createReview(
                    customerDetails.getId(),
                    request.getProductId(),
                    request.getRating(),
                    request.getText()
            );
            return ResponseEntity.ok(Map.of("id", review.getId(), "message", "Review created"));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }
}