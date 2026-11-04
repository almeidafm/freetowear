package com.freetowear.service;

import com.freetowear.entity.Customer;
import com.freetowear.entity.Product;
import com.freetowear.entity.ProductVariation;
import com.freetowear.entity.Review;
import com.freetowear.entity.ReviewImage;
import com.freetowear.enums.OrderStatus;
import com.freetowear.enums.Size;
import com.freetowear.dto.response.review.ReviewResponse;
import com.freetowear.infra.CloudinaryService;
import com.freetowear.repository.CustomerRepository;
import com.freetowear.repository.OrderItemRepository;
import com.freetowear.repository.ProductRepository;
import com.freetowear.repository.ReviewRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private CloudinaryService cloudinaryService;
    @InjectMocks private ReviewService reviewService;

    private Customer customer() {
        Customer customer = new Customer();
        customer.setId("customer-1");
        customer.setName("Jane Doe");
        return customer;
    }

    private Product product() {
        Product product = new Product();
        product.setId("product-1");
        product.setName("T-Shirt");
        product.setRatingAverage(new BigDecimal("4.00"));
        product.setRatingCount(2);
        return product;
    }

    @Nested
    class CreateReviewTests {
        @Test
        void shouldCreateReviewAndUpdateProductRating() {
            Customer customer = customer();
            Product product = product();
            when(customerRepository.findById("customer-1")).thenReturn(Optional.of(customer));
            when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
            when(orderItemRepository.existsByOrderCustomerIdAndProductIdAndOrderStatusIn(
                    eq("customer-1"), eq("product-1"), any())).thenReturn(true);
            when(reviewRepository.existsByCustomerIdAndProductId("customer-1", "product-1"))
                    .thenReturn(false);

            Review result = reviewService.createReview("customer-1", "product-1", 5, "Great shirt");

            assertThat(result.getCustomer()).isSameAs(customer);
            assertThat(result.getProduct()).isSameAs(product);
            assertThat(result.getRating()).isEqualTo(5);
            assertThat(result.getText()).isEqualTo("Great shirt");
            assertThat(product.getRatingCount()).isEqualTo(3);
            assertThat(product.getRatingAverage()).isEqualByComparingTo("4.33");
            verify(reviewRepository).save(result);
            verify(productRepository).save(product);
        }

        @Test
        void shouldRejectInvalidRatingWithoutCallingRepositories() {
            assertThatThrownBy(() -> reviewService.createReview("customer-1", "product-1", 6, "text"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rating must be between 1 and 5");

            verify(customerRepository, never()).findById(any());
            verify(reviewRepository, never()).save(any());
        }

        @Test
        void shouldRejectReviewWhenProductWasNotPurchased() {
            when(customerRepository.findById("customer-1")).thenReturn(Optional.of(customer()));
            when(productRepository.findById("product-1")).thenReturn(Optional.of(product()));
            when(orderItemRepository.existsByOrderCustomerIdAndProductIdAndOrderStatusIn(
                    eq("customer-1"), eq("product-1"), any())).thenReturn(false);

            assertThatThrownBy(() -> reviewService.createReview("customer-1", "product-1", 4, "text"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Customer has not purchased this product");
            verify(reviewRepository, never()).save(any());
        }

        @Test
        void shouldRejectDuplicateReview() {
            when(customerRepository.findById("customer-1")).thenReturn(Optional.of(customer()));
            when(productRepository.findById("product-1")).thenReturn(Optional.of(product()));
            when(orderItemRepository.existsByOrderCustomerIdAndProductIdAndOrderStatusIn(
                    eq("customer-1"), eq("product-1"), any())).thenReturn(true);
            when(reviewRepository.existsByCustomerIdAndProductId("customer-1", "product-1"))
                    .thenReturn(true);

            assertThatThrownBy(() -> reviewService.createReview("customer-1", "product-1", 4, "text"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Customer has already reviewed this product");
            verify(reviewRepository, never()).save(any());
        }
    }

    @Test
    void shouldListReviewsWithImagesAndPurchasedVariations() {
        Review review = new Review();
        review.setCustomer(customer());
        review.setRating(5);
        review.setText("Excellent");
        ReviewImage image = new ReviewImage();
        image.setImagePublicId("reviews/image-1");
        review.setImages(List.of(image));

        ProductVariation variation = new ProductVariation();
        variation.setColor("Blue");
        variation.setSize(Size.M);
        when(reviewRepository.findAllByProductIdOrderByCreatedAtDesc("product-1"))
                .thenReturn(List.of(review));
        when(cloudinaryService.buildUrl("reviews/image-1")).thenReturn("https://image/1");
        when(orderItemRepository.findPurchasedVariationsByCustomerAndProduct(
                eq("customer-1"), eq("product-1"), any())).thenReturn(List.of(variation));

        List<ReviewResponse> result = reviewService.findAllByProductId("product-1");

        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.getCustomerName()).isEqualTo("Jane Doe");
            assertThat(response.getRating()).isEqualTo(5);
            assertThat(response.getImageUrls()).containsExactly("https://image/1");
            assertThat(response.getPurchasedVariations()).containsExactly("Blue / M");
        });
    }

    @Test
    void shouldUploadNonEmptyImagesWhenCreatingReviewWithImages() throws Exception {
        when(customerRepository.findById("customer-1")).thenReturn(Optional.of(customer()));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product()));
        when(orderItemRepository.existsByOrderCustomerIdAndProductIdAndOrderStatusIn(
                eq("customer-1"), eq("product-1"), any())).thenReturn(true);
        when(reviewRepository.existsByCustomerIdAndProductId("customer-1", "product-1"))
                .thenReturn(false);
        when(cloudinaryService.uploadPublic(any(), eq("reviews"))).thenReturn("reviews/photo");

        reviewService.createReview("customer-1", "product-1", 5, "Nice", List.of(
                new MockMultipartFile("image", "photo.png", "image/png", new byte[]{1}),
                new MockMultipartFile("empty", "", "image/png", new byte[0])));

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        Review saved = captor.getAllValues().get(1);
        assertThat(saved.getImages()).singleElement().satisfies(image -> {
            assertThat(image.getImagePublicId()).isEqualTo("reviews/photo");
            assertThat(image.getDisplayOrder()).isZero();
            assertThat(image.getReview()).isSameAs(saved);
        });
    }
}