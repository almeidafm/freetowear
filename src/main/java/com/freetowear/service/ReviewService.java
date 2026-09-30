package com.freetowear.service;

import com.freetowear.entity.Customer;
import com.freetowear.entity.Product;
import com.freetowear.entity.Review;
import com.freetowear.entity.ReviewImage;
import com.freetowear.enums.OrderStatus;
import com.freetowear.repository.CustomerRepository;
import com.freetowear.repository.OrderItemRepository;
import com.freetowear.repository.ProductRepository;
import com.freetowear.repository.ReviewRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import com.freetowear.infra.CloudinaryService;

@Service
public class ReviewService {

    private static final EnumSet<OrderStatus> COMPLETED_ORDER_STATUSES = EnumSet.of(
            OrderStatus.PAID,
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED
    );

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final CloudinaryService cloudinaryService;

    public ReviewService(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            OrderItemRepository orderItemRepository,
            ReviewRepository reviewRepository,
            CloudinaryService cloudinaryService
    ) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.reviewRepository = reviewRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public Review createReview(String customerId, String productId, Integer rating, String text,
                               List<MultipartFile> images) throws IOException {
        Review review = createReview(customerId, productId, rating, text);
        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) continue;
            ReviewImage reviewImage = new ReviewImage();
            reviewImage.setReview(review);
            reviewImage.setImagePublicId(cloudinaryService.uploadPublic(image, "reviews"));
            reviewImage.setDisplayOrder(review.getImages() == null ? 0 : review.getImages().size());
            if (review.getImages() == null) review.setImages(new java.util.ArrayList<>());
            review.getImages().add(reviewImage);
        }
        return reviewRepository.save(review);
    }

    @Transactional
    public Review createReview(String customerId, String productId, Integer rating, String text) {
        validateRating(rating);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        boolean purchased = orderItemRepository.existsByOrderCustomerIdAndProductIdAndOrderStatusIn(
                customerId,
                productId,
                COMPLETED_ORDER_STATUSES
        );
        if (!purchased) {
            throw new IllegalStateException("Customer has not purchased this product");
        }

        if (reviewRepository.existsByCustomerIdAndProductId(customerId, productId)) {
            throw new IllegalStateException("Customer has already reviewed this product");
        }

        Review review = new Review();
        review.setCustomer(customer);
        review.setProduct(product);
        review.setRating(rating);
        review.setText(text);
        reviewRepository.save(review);

        updateProductRating(product, rating);
        return review;
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    private void updateProductRating(Product product, Integer rating) {
        int previousCount = product.getRatingCount() == null ? 0 : product.getRatingCount();
        BigDecimal previousAverage = product.getRatingAverage() == null
                ? BigDecimal.ZERO
                : product.getRatingAverage();
        BigDecimal totalRating = previousAverage.multiply(BigDecimal.valueOf(previousCount))
                .add(BigDecimal.valueOf(rating));

        product.setRatingCount(previousCount + 1);
        product.setRatingAverage(totalRating.divide(
                BigDecimal.valueOf(previousCount + 1),
                2,
                RoundingMode.HALF_UP
        ));
        productRepository.save(product);
    }
}
