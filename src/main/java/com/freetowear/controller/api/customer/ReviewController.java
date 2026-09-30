package com.freetowear.controller.api.customer;

import com.freetowear.dto.request.review.CreateReviewRequest;
import com.freetowear.entity.Review;
import com.freetowear.infra.security.CustomerDetails;
import com.freetowear.service.ReviewService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

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
            @Valid @RequestParam String productId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<MultipartFile> images
    ) {
        try {
            Review review = reviewService.createReview(
                    customerDetails.getId(),
                    productId, rating, text, images == null ? List.of() : images
            );
            return ResponseEntity.ok(Map.of("id", review.getId(), "message", "Review created"));
        } catch (IllegalArgumentException | IllegalStateException | IOException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }
}
