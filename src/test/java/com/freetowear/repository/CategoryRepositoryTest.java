package com.freetowear.repository;

import com.freetowear.entity.Category;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryRepositoryTest extends RepositoryTestBase {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnCategoryByNameUsingMySqlDefaultCaseInsensitiveCollation() {
        Category expected = persistCategory("Shoes", true);
        flushAndClear();

        var result = categoryRepository.findByName("shoes");

        assertThat(result).isPresent().get().extracting(Category::getId).isEqualTo(expected.getId());
    }

    @Test
    void shouldReturnEmptyWhenCategoryNameDoesNotExist() {
        persistCategory("Shoes", true);
        flushAndClear();

        assertThat(categoryRepository.findByName("Pants")).isEmpty();
    }

    @Test
    void shouldReturnCategoryByNameIgnoringCase() {
        Category expected = persistCategory("Shoes", true);
        flushAndClear();

        var result = categoryRepository.findByNameIgnoreCase("sHoEs");

        assertThat(result).isPresent().get().extracting(Category::getId).isEqualTo(expected.getId());
    }

    @Test
    void shouldReturnEmptyWhenCaseInsensitiveCategoryNameDoesNotExist() {
        persistCategory("Shoes", true);
        flushAndClear();

        assertThat(categoryRepository.findByNameIgnoreCase("PANTS")).isEmpty();
    }

    @Test
    void shouldReturnCategoriesContainingNameIgnoringCase() {
        Category shoes = persistCategory("Running Shoes", true);
        persistCategory("Winter Boots", false);
        Category shirts = persistCategory("Shirts", true);
        flushAndClear();

        List<Category> result = categoryRepository.findByNameContainingIgnoreCase("sH");

        assertThat(result).extracting(Category::getId)
                .containsExactlyInAnyOrder(shoes.getId(), shirts.getId());
    }

    @Test
    void shouldReturnEmptyWhenNoCategoryContainsName() {
        persistCategory("Shoes", true);
        flushAndClear();

        assertThat(categoryRepository.findByNameContainingIgnoreCase("pants")).isEmpty();
    }

    @Test
    void shouldReturnOnlyActiveCategories() {
        Category active = persistCategory("Shoes", true);
        persistCategory("Pants", false);
        flushAndClear();

        List<Category> result = categoryRepository.findByActiveTrue();

        assertThat(result).extracting(Category::getId).containsExactly(active.getId());
    }

    @Test
    void shouldReturnEmptyWhenNoCategoriesAreActive() {
        persistCategory("Shoes", false);
        persistCategory("Pants", false);
        flushAndClear();

        assertThat(categoryRepository.findByActiveTrue()).isEmpty();
    }

    @Test
    void shouldRejectDuplicateCategoryNames() {
        persistCategory("Shoes", true);
        persistCategory("Shoes", false);

        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(RuntimeException.class);
    }

    private Category persistCategory(String name, boolean active) {
        Category category = new Category();
        category.setName(name);
        category.setActive(active);
        entityManager.persist(category);
        return category;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}