package com.freetowear.repository;

import com.freetowear.entity.Category;
import com.freetowear.entity.Customer;
import com.freetowear.entity.Product;
import com.freetowear.entity.Review;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewRepositoryTest extends RepositoryTestBase {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnTrueWhenCustomerAlreadyReviewedProduct() {
        Customer customer = persistCustomer("customer@mail.com");
        Product product = persistProduct("Shirt", "Clothing");
        persistReview(customer, product, 5, LocalDateTime.of(2026, 1, 1, 12, 0));
        flushAndClear();

        assertThat(reviewRepository.existsByCustomerIdAndProductId(customer.getId(), product.getId())).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCustomerHasNotReviewedProduct() {
        Customer customer = persistCustomer("customer@mail.com");
        Product product = persistProduct("Shirt", "Clothing");
        flushAndClear();

        assertThat(reviewRepository.existsByCustomerIdAndProductId(customer.getId(), product.getId())).isFalse();
    }

    @Test
    void shouldReturnEmptyWhenCustomerAndProductReviewDoesNotExist() {
        Customer customer = persistCustomer("customer@mail.com");
        Customer otherCustomer = persistCustomer("other@mail.com");
        Product product = persistProduct("Shirt", "Clothing");
        persistReview(otherCustomer, product, 4, LocalDateTime.now());
        flushAndClear();

        assertThat(reviewRepository.findByCustomerIdAndProductId(customer.getId(), product.getId())).isEmpty();
    }

    @Test
    void shouldReturnReviewForCustomerAndProduct() {
        Customer customer = persistCustomer("customer@mail.com");
        Product product = persistProduct("Shirt", "Clothing");
        Review expected = persistReview(customer, product, 5, LocalDateTime.of(2026, 1, 1, 12, 0));
        flushAndClear();

        assertThat(reviewRepository.findByCustomerIdAndProductId(customer.getId(), product.getId()))
                .isPresent().get().satisfies(review -> {
                    assertThat(review.getId()).isEqualTo(expected.getId());
                    assertThat(review.getRating()).isEqualTo(5);
                });
    }

    @Test
    void shouldReturnEmptyWhenProductHasNoReviews() {
        Product product = persistProduct("Shirt", "Clothing");
        flushAndClear();

        assertThat(reviewRepository.findAllByProductIdOrderByCreatedAtDesc(product.getId())).isEmpty();
    }

    @Test
    void shouldReturnOnlyReviewsForRequestedProduct() {
        Customer customer = persistCustomer("customer@mail.com");
        Product product = persistProduct("Shirt", "Clothing");
        Product otherProduct = persistProduct("Pants", "Bottoms");
        Review expected = persistReview(customer, product, 5, LocalDateTime.of(2026, 1, 2, 12, 0));
        persistReview(persistCustomer("other@mail.com"), otherProduct, 3, LocalDateTime.of(2026, 1, 3, 12, 0));
        flushAndClear();

        assertThat(reviewRepository.findAllByProductIdOrderByCreatedAtDesc(product.getId()))
                .extracting(Review::getId).containsExactly(expected.getId());
    }

    @Test
    void shouldReturnMultipleProductReviewsNewestFirst() {
        Customer firstCustomer = persistCustomer("first@mail.com");
        Customer secondCustomer = persistCustomer("second@mail.com");
        Product product = persistProduct("Shirt", "Clothing");
        Review oldest = persistReview(firstCustomer, product, 3, LocalDateTime.of(2026, 1, 1, 12, 0));
        Review newest = persistReview(secondCustomer, product, 5, LocalDateTime.of(2026, 1, 3, 12, 0));
        Review middle = persistReview(persistCustomer("third@mail.com"), product, 4,
                LocalDateTime.of(2026, 1, 2, 12, 0));
        flushAndClear();

        assertThat(reviewRepository.findAllByProductIdOrderByCreatedAtDesc(product.getId()))
                .extracting(Review::getId)
                .containsExactly(newest.getId(), middle.getId(), oldest.getId());
    }

    @Test
    void shouldReturnReviewsCreatedBetweenInclusiveBoundaries() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 28, 23, 59);
        Product product = persistProduct("Shirt", "Clothing");
        Review atStart = persistReview(persistCustomer("first@mail.com"), product, 5, start);
        Review atEnd = persistReview(persistCustomer("second@mail.com"), product, 4, end);
        persistReview(persistCustomer("before@mail.com"), product, 3, start.minusSeconds(1));
        persistReview(persistCustomer("after@mail.com"), product, 2, end.plusSeconds(1));
        flushAndClear();

        assertThat(reviewRepository.findByCreatedAtBetween(start, end))
                .extracting(Review::getId).containsExactlyInAnyOrder(atStart.getId(), atEnd.getId());
    }

    @Test
    void shouldReturnReviewsCreatedAtOrAfterBoundary() {
        LocalDateTime boundary = LocalDateTime.of(2026, 3, 1, 0, 0);
        Product product = persistProduct("Shirt", "Clothing");
        Review atBoundary = persistReview(persistCustomer("boundary@mail.com"), product, 5, boundary);
        Review after = persistReview(persistCustomer("after@mail.com"), product, 4, boundary.plusSeconds(1));
        persistReview(persistCustomer("before@mail.com"), product, 3, boundary.minusSeconds(1));
        flushAndClear();

        assertThat(reviewRepository.findByCreatedAtGreaterThanEqual(boundary))
                .extracting(Review::getId).containsExactlyInAnyOrder(atBoundary.getId(), after.getId());
    }

    @Test
    void shouldReturnEmptyWhenNoReviewsMatchCreatedAtRange() {
        Product product = persistProduct("Shirt", "Clothing");
        persistReview(persistCustomer("customer@mail.com"), product, 5,
                LocalDateTime.of(2026, 1, 1, 12, 0));
        flushAndClear();

        assertThat(reviewRepository.findByCreatedAtBetween(
                LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 3, 1, 0, 0))).isEmpty();
    }

    @Test
    void shouldRejectDuplicateCustomerProductReviews() {
        Customer customer = persistCustomer("customer@mail.com");
        Product product = persistProduct("Shirt", "Clothing");
        persistReview(customer, product, 5, LocalDateTime.now());
        persistReview(customer, product, 4, LocalDateTime.now());

        assertThatThrownBy(() -> entityManager.flush()).isInstanceOf(RuntimeException.class);
    }

    private Customer persistCustomer(String email) {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail(email);
        customer.setPassword("password");
        customer.setTermsAndConditionsAcceptedAt(LocalDateTime.now());
        entityManager.persist(customer);
        return customer;
    }

    private Product persistProduct(String name, String categoryName) {
        Category category = new Category();
        category.setName(categoryName);
        entityManager.persist(category);

        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(new BigDecimal("49.90"));
        entityManager.persist(product);
        return product;
    }

    private Review persistReview(Customer customer, Product product, int rating, LocalDateTime createdAt) {
        Review review = new Review();
        review.setCustomer(customer);
        review.setProduct(product);
        review.setRating(rating);
        review.setText("Test review");
        review.setCreatedAt(createdAt);
        review.setUpdatedAt(createdAt);
        entityManager.persist(review);
        return review;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}