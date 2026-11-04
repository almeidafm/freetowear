package com.freetowear.service;

import com.freetowear.dto.response.review.ReviewResponse;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import com.freetowear.infra.CloudinaryService;

@Service
public class ReviewService {

    private static final int MAX_REVIEW_TEXT_LENGTH = 2000;
    private static final int MAX_IMAGES = 5;
    private static final long MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    private static final EnumSet<OrderStatus> COMPLETED_ORDER_STATUSES = EnumSet.of(
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

    @Transactional(readOnly = true)
    public List<ReviewResponse> findAllByProductId(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new com.freetowear.exception.ResourceNotFoundException("Product not found");
        }
        return reviewRepository.findAllByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(review -> {
                    List<String> imageUrls = review.getImages() == null
                            ? List.of()
                            : review.getImages().stream()
                                    .map(img -> cloudinaryService.buildUrl(img.getImagePublicId()))
                                    .toList();
                    List<String> purchasedVariations = orderItemRepository
                            .findPurchasedVariationsByCustomerAndProduct(
                                    review.getCustomer().getId(),
                                    productId,
                                    COMPLETED_ORDER_STATUSES
                            )
                            .stream()
                            .map(v -> v.getColor() + " / " + v.getSize().name())
                            .toList();
                    return new ReviewResponse(review, imageUrls, purchasedVariations);
                })
                .toList();
    }

    @Transactional(rollbackFor = IOException.class)
    public Review createReview(String customerId, String productId, Integer rating, String text,
                               List<MultipartFile> images) throws IOException {
        validateText(text);
        validateImages(images);
        Review review = createReview(customerId, productId, rating, text);
        List<String> uploadedIds = new java.util.ArrayList<>();
        try {
            for (MultipartFile image : images == null ? List.<MultipartFile>of() : images) {
            if (image == null || image.isEmpty()) continue;
            ReviewImage reviewImage = new ReviewImage();
            reviewImage.setReview(review);
            String publicId = cloudinaryService.uploadPublic(image, "reviews");
            uploadedIds.add(publicId);
            reviewImage.setImagePublicId(publicId);
            reviewImage.setDisplayOrder(review.getImages() == null ? 0 : review.getImages().size());
            if (review.getImages() == null) review.setImages(new java.util.ArrayList<>());
            review.getImages().add(reviewImage);
            }
            return reviewRepository.save(review);
        } catch (RuntimeException | IOException exception) {
            for (String uploadedId : uploadedIds) {
                try {
                    cloudinaryService.delete(uploadedId);
                } catch (IOException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            throw exception;
        }
    }

    @Transactional
    public Review createReview(String customerId, String productId, Integer rating, String text) {
        validateRating(rating);
        validateText(text);

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
        try {
            reviewRepository.save(review);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("Customer has already reviewed this product", exception);
        }

        updateProductRating(product, rating);
        return review;
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    private void validateText(String text) {
        if (text != null && (text.isBlank() || text.length() > MAX_REVIEW_TEXT_LENGTH)) {
            throw new IllegalArgumentException("Review text must be between 1 and 2000 characters");
        }
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null) return;
        long imageCount = images.stream().filter(image -> image != null && !image.isEmpty()).count();
        if (imageCount > MAX_IMAGES) throw new IllegalArgumentException("A review can contain at most 5 images");
        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) continue;
            if (image.getSize() > MAX_IMAGE_BYTES) throw new IllegalArgumentException("Review image is too large");
            if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("Review images must be image files");
            }
        }
    }

    private synchronized void updateProductRating(Product product, Integer rating) {
        int previousCount = product.getRatingCount() == null || product.getRatingCount() < 0
                ? 0 : product.getRatingCount();
        BigDecimal previousAverage = product.getRatingAverage() == null
                ? BigDecimal.ZERO
                : product.getRatingAverage();
        if (previousAverage.signum() < 0 || previousAverage.compareTo(BigDecimal.valueOf(5)) > 0) {
            previousAverage = BigDecimal.ZERO;
        }
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