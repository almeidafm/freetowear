package com.freetowear.repository;

import com.freetowear.entity.Category;
import com.freetowear.entity.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryTest extends RepositoryTestBase {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnOnlyActiveProducts() {
        Category clothing = persistCategory("Clothing");
        Product active = persistProduct("Active Shirt", clothing, true, "49.90");
        persistProduct("Inactive Shirt", clothing, false, "39.90");
        flushAndClear();

        assertThat(productRepository.findByActiveTrue()).singleElement().satisfies(product -> {
            assertThat(product.getId()).isEqualTo(active.getId());
            assertThat(product.getPrice()).isEqualByComparingTo("49.90");
        });
    }

    @Test
    void shouldReturnEmptyWhenNoProductsAreActive() {
        persistProduct("Inactive Shirt", "Clothing", false, "39.90");
        flushAndClear();

        assertThat(productRepository.findByActiveTrue()).isEmpty();
    }

    @Test
    void shouldReturnOnlyActiveProductsFromRequestedCategory() {
        Category clothing = persistCategory("Clothing");
        Category shoes = persistCategory("Shoes");
        Product expected = persistProduct("Active Shirt", clothing, true, "49.90");
        persistProduct("Inactive Shirt", clothing, false, "39.90");
        persistProduct("Active Shoes", shoes, true, "89.90");
        flushAndClear();

        assertThat(productRepository.findByCategoryIdAndActiveTrue(clothing.getId()))
                .singleElement().satisfies(product -> {
                    assertThat(product.getId()).isEqualTo(expected.getId());
                    assertThat(product.getCategory().getId()).isEqualTo(clothing.getId());
                    assertThat(product.getPrice()).isEqualByComparingTo("49.90");
                });
    }

    @Test
    void shouldReturnEmptyWhenCategoryHasNoActiveProducts() {
        Category clothing = persistCategory("Clothing");
        persistProduct("Inactive Shirt", clothing, false, "39.90");
        flushAndClear();

        assertThat(productRepository.findByCategoryIdAndActiveTrue(clothing.getId())).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenCategoryDoesNotExist() {
        persistProduct("Active Shirt", "Clothing", true, "49.90");
        flushAndClear();

        assertThat(productRepository.findByCategoryIdAndActiveTrue("01J00000000000000000000000")).isEmpty();
    }

    @Test
    void shouldReturnProductsContainingNameIgnoringCase() {
        Product first = persistProduct("Classic Shirt", "Clothing", true, "49.90");
        Product second = persistProduct("Shirt Jacket", "Outerwear", false, "79.90");
        persistProduct("Leather Shoes", "Shoes", true, "99.90");
        flushAndClear();

        assertThat(productRepository.findByNameContainingIgnoreCase("sHiRt"))
                .extracting(Product::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void shouldReturnInactiveProductsWhenNameMatchesBecauseNameSearchDoesNotFilterActiveState() {
        Product inactive = persistProduct("Archived Shirt", "Clothing", false, "29.90");
        flushAndClear();

        assertThat(productRepository.findByNameContainingIgnoreCase("shirt"))
                .singleElement().satisfies(product -> {
                    assertThat(product.getId()).isEqualTo(inactive.getId());
                    assertThat(product.getActive()).isFalse();
                    assertThat(product.getPrice()).isEqualByComparingTo("29.90");
                });
    }

    @Test
    void shouldReturnEmptyWhenNoProductNameMatches() {
        persistProduct("Classic Shirt", "Clothing", true, "49.90");
        flushAndClear();

        assertThat(productRepository.findByNameContainingIgnoreCase("pants")).isEmpty();
    }

    private Category persistCategory(String name) {
        Category category = new Category();
        category.setName(name);
        entityManager.persist(category);
        return category;
    }

    private Product persistProduct(String name, String categoryName, boolean active, String price) {
        return persistProduct(name, persistCategory(categoryName), active, price);
    }

    private Product persistProduct(String name, Category category, boolean active, String price) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setActive(active);
        product.setPrice(new BigDecimal(price));
        entityManager.persist(product);
        return product;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}