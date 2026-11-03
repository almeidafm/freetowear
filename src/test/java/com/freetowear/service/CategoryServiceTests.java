package com.freetowear.service;

import com.freetowear.dto.request.category.CreateCategoryRequest;
import com.freetowear.dto.request.category.UpdateCategoryRequest;
import com.freetowear.dto.response.category.CategoryResponse;
import com.freetowear.entity.Category;
import com.freetowear.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    private static CategoryBuilder aCategory() {
        return new CategoryBuilder();
    }

    private static final class CategoryBuilder {
        private final Category category = new Category();

        CategoryBuilder withId(String id) {
            category.setId(id);
            return this;
        }

        CategoryBuilder withName(String name) {
            category.setName(name);
            return this;
        }

        CategoryBuilder withActive(Boolean active) {
            category.setActive(active);
            return this;
        }

        Category build() {
            return category;
        }
    }

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Nested
    @DisplayName("createCategory")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should save category with request name")
        void shouldCreateCategoryWhenRequestIsValid() {
            categoryService.createCategory(new CreateCategoryRequest("Shirts"));

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            verify(categoryRepository).save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("Shirts");
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update name and active status when category exists")
        void shouldUpdateCategoryWhenCategoryExists() {
            Category category = aCategory().withId("category-1").withName("Old name")
                    .withActive(true).build();
            when(categoryRepository.findById("category-1")).thenReturn(Optional.of(category));

            categoryService.updateCategory("category-1", new UpdateCategoryRequest("New name", false));

            assertThat(category.getName()).isEqualTo("New name");
            assertThat(category.getActive()).isFalse();
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should update only active status when name is null or empty")
        void shouldUpdateActiveWhenNameIsBlank() {
            Category category = aCategory().withName("Keep name").withActive(true).build();
            when(categoryRepository.findById("category-1")).thenReturn(Optional.of(category));

            categoryService.updateCategory("category-1", new UpdateCategoryRequest("", false));

            assertThat(category.getName()).isEqualTo("Keep name");
            assertThat(category.getActive()).isFalse();
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should update only name when active status is null")
        void shouldUpdateNameWhenActiveIsNull() {
            Category category = aCategory().withName("Old name").withActive(true).build();
            when(categoryRepository.findById("category-1")).thenReturn(Optional.of(category));

            categoryService.updateCategory("category-1", new UpdateCategoryRequest("New name", null));

            assertThat(category.getName()).isEqualTo("New name");
            assertThat(category.getActive()).isTrue();
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should save unchanged category when all update fields are null")
        void shouldSaveUnchangedCategoryWhenUpdateFieldsAreNull() {
            Category category = aCategory().withName("Keep name").withActive(true).build();
            when(categoryRepository.findById("category-1")).thenReturn(Optional.of(category));

            categoryService.updateCategory("category-1", new UpdateCategoryRequest(null, null));

            assertThat(category.getName()).isEqualTo("Keep name");
            assertThat(category.getActive()).isTrue();
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should not save when category does not exist")
        void shouldNotUpdateCategoryWhenCategoryDoesNotExist() {
            when(categoryRepository.findById("missing")).thenReturn(Optional.empty());

            categoryService.updateCategory("missing", new UpdateCategoryRequest("New name", false));

            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("Should map every category field")
        void shouldReturnAllCategoriesWhenCategoriesExist() {
            Category first = aCategory().withId("category-1").withName("Shirts").withActive(true).build();
            Category second = aCategory().withId("category-2").withName("Pants").withActive(false).build();
            when(categoryRepository.findAll()).thenReturn(List.of(first, second));

            List<CategoryResponse> responses = categoryService.getAllCategories();

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getId()).isEqualTo("category-1");
            assertThat(responses.get(0).getName()).isEqualTo("Shirts");
            assertThat(responses.get(0).getActive()).isTrue();
            assertThat(responses.get(1).getId()).isEqualTo("category-2");
            assertThat(responses.get(1).getName()).isEqualTo("Pants");
            assertThat(responses.get(1).getActive()).isFalse();
        }

        @Test
        @DisplayName("Should return empty list when no categories exist")
        void shouldReturnEmptyListWhenNoCategoriesExist() {
            when(categoryRepository.findAll()).thenReturn(List.of());

            assertThat(categoryService.getAllCategories()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCategoryById")
    class GetCategoryByIdTests {

        @Test
        @DisplayName("Should return mapped category when category exists")
        void shouldReturnCategoryWhenCategoryExists() {
            Category category = aCategory().withId("category-1").withName("Shirts")
                    .withActive(true).build();
            when(categoryRepository.findById("category-1")).thenReturn(Optional.of(category));

            Optional<CategoryResponse> response = categoryService.getCategoryById("category-1");

            assertThat(response).isPresent();
            assertThat(response.get().getId()).isEqualTo("category-1");
            assertThat(response.get().getName()).isEqualTo("Shirts");
            assertThat(response.get().getActive()).isTrue();
        }

        @Test
        @DisplayName("Should return empty optional when category does not exist")
        void shouldReturnEmptyWhenCategoryDoesNotExist() {
            when(categoryRepository.findById("missing")).thenReturn(Optional.empty());

            assertThat(categoryService.getCategoryById("missing")).isEmpty();
        }
    }
}