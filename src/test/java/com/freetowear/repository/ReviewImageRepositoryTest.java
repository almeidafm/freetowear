package com.freetowear.repository;

import com.freetowear.entity.Category;
import com.freetowear.entity.Customer;
import com.freetowear.entity.Product;
import com.freetowear.entity.Review;
import com.freetowear.entity.ReviewImage;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewImageRepositoryTest extends RepositoryTestBase {

    @Autowired
    private ReviewImageRepository reviewImageRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnEmptyWhenReviewHasNoImages() {
        Review review = persistReview("customer@mail.com", "Shirt", "Clothing");
        flushAndClear();

        assertThat(reviewImageRepository.findAllByReviewIdOrderByDisplayOrderAsc(review.getId())).isEmpty();
    }

    @Test
    void shouldReturnOnlyImagesBelongingToRequestedReview() {
        Review review = persistReview("customer@mail.com", "Shirt", "Clothing");
        Review otherReview = persistReview("other@mail.com", "Pants", "Bottoms");
        ReviewImage expected = persistImage(review, "review-image-1", 1);
        persistImage(otherReview, "other-image-1", 1);
        flushAndClear();

        assertThat(reviewImageRepository.findAllByReviewIdOrderByDisplayOrderAsc(review.getId()))
                .extracting(ReviewImage::getId).containsExactly(expected.getId());
    }

    @Test
    void shouldReturnMultipleImagesForReviewInDisplayOrder() {
        Review review = persistReview("customer@mail.com", "Shirt", "Clothing");
        ReviewImage last = persistImage(review, "image-last", 3);
        ReviewImage first = persistImage(review, "image-first", 1);
        ReviewImage middle = persistImage(review, "image-middle", 2);
        flushAndClear();

        assertThat(reviewImageRepository.findAllByReviewIdOrderByDisplayOrderAsc(review.getId()))
                .extracting(ReviewImage::getId)
                .containsExactly(first.getId(), middle.getId(), last.getId());
    }

    @Test
    void shouldReturnImagesWithIdenticalDisplayOrder() {
        Review review = persistReview("customer@mail.com", "Shirt", "Clothing");
        ReviewImage first = persistImage(review, "image-first", 1);
        ReviewImage second = persistImage(review, "image-second", 1);
        flushAndClear();

        assertThat(reviewImageRepository.findAllByReviewIdOrderByDisplayOrderAsc(review.getId()))
                .extracting(ReviewImage::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void shouldReturnEmptyWhenReviewIdDoesNotExist() {
        Review review = persistReview("customer@mail.com", "Shirt", "Clothing");
        persistImage(review, "image-1", 1);
        flushAndClear();

        assertThat(reviewImageRepository.findAllByReviewIdOrderByDisplayOrderAsc(
                "01J00000000000000000000000")).isEmpty();
    }

    private Review persistReview(String email, String productName, String categoryName) {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail(email);
        customer.setPassword("password");
        customer.setTermsAndConditionsAcceptedAt(LocalDateTime.now());
        entityManager.persist(customer);

        Category category = new Category();
        category.setName(categoryName);
        entityManager.persist(category);

        Product product = new Product();
        product.setName(productName);
        product.setCategory(category);
        product.setPrice(new BigDecimal("49.90"));
        entityManager.persist(product);

        Review review = new Review();
        review.setCustomer(customer);
        review.setProduct(product);
        review.setRating(5);
        review.setText("Test review");
        entityManager.persist(review);
        return review;
    }

    private ReviewImage persistImage(Review review, String publicId, int displayOrder) {
        ReviewImage image = new ReviewImage();
        image.setReview(review);
        image.setImagePublicId(publicId);
        image.setDisplayOrder(displayOrder);
        entityManager.persist(image);
        return image;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}