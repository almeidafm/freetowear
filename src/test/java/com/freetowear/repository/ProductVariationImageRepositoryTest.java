package com.freetowear.repository;

import com.freetowear.entity.Category;
import com.freetowear.entity.Product;
import com.freetowear.entity.ProductVariation;
import com.freetowear.entity.ProductVariationImage;
import com.freetowear.enums.Size;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ProductVariationImageRepositoryTest extends RepositoryTestBase {

    @Autowired private ProductVariationImageRepository imageRepository;
    @Autowired private EntityManager entityManager;

    private final AtomicInteger categorySequence = new AtomicInteger();

    @Test
    void shouldReturnVariationImagesInDisplayOrder() {
        ProductVariation variation = persistVariation();
        ProductVariationImage last = persistImage(variation, "last", 2);
        ProductVariationImage first = persistImage(variation, "first", 0);
        ProductVariationImage middle = persistImage(variation, "middle", 1);
        entityManager.flush();
        entityManager.clear();

        assertThat(imageRepository.findAllByProductVariationIdOrderByDisplayOrderAsc(variation.getId()))
                .extracting(ProductVariationImage::getImagePublicId)
                .containsExactly("first", "middle", "last");
    }

    @Test
    void shouldReturnOnlyImagesForRequestedVariation() {
        ProductVariation variation = persistVariation();
        ProductVariation otherVariation = persistVariation();
        persistImage(variation, "expected", 0);
        persistImage(otherVariation, "other", 0);
        entityManager.flush();
        entityManager.clear();

        assertThat(imageRepository.findAllByProductVariationIdOrderByDisplayOrderAsc(variation.getId()))
                .extracting(ProductVariationImage::getImagePublicId)
                .containsExactly("expected");
    }

    private ProductVariation persistVariation() {
        Category category = new Category();
        category.setName("Clothing-" + categorySequence.incrementAndGet());
        entityManager.persist(category);

        Product product = new Product();
        product.setName("Shirt");
        product.setCategory(category);
        product.setPrice(new BigDecimal("49.90"));
        entityManager.persist(product);

        ProductVariation variation = new ProductVariation();
        variation.setProduct(product);
        variation.setColor("Blue");
        variation.setSize(Size.M);
        variation.setStock(10);
        entityManager.persist(variation);
        return variation;
    }

    private ProductVariationImage persistImage(ProductVariation variation, String publicId, int order) {
        ProductVariationImage image = new ProductVariationImage();
        image.setProductVariation(variation);
        image.setImagePublicId(publicId);
        image.setDisplayOrder(order);
        entityManager.persist(image);
        return image;
    }
}