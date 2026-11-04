package com.freetowear.service;

import com.freetowear.dto.request.product.CreateProductRequest;
import com.freetowear.dto.request.product.UpdateProductRequest;
import com.freetowear.dto.response.product.ProductResponse;
import com.freetowear.entity.Category;
import com.freetowear.entity.Product;
import com.freetowear.entity.ProductVariation;
import com.freetowear.enums.Size;
import com.freetowear.exception.ResourceNotFoundException;
import com.freetowear.infra.CloudinaryService;
import com.freetowear.repository.CategoryRepository;
import com.freetowear.repository.ProductRepository;
import com.freetowear.repository.ProductVariationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductVariationRepository productVariationRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CloudinaryService cloudinaryService;
    @InjectMocks private ProductService productService;

    private MockMultipartFile image() {
        return new MockMultipartFile("image", "shirt.png", "image/png", new byte[]{1});
    }

    private Category category() {
        Category category = new Category();
        category.setId("category-1");
        category.setName("Shirts");
        return category;
    }

    private ProductVariation variation() {
        ProductVariation variation = new ProductVariation();
        variation.setId("variation-1");
        variation.setColor("Blue");
        variation.setSize(Size.M);
        variation.setStock(10);
        return variation;
    }

    private Product product() {
        Product product = new Product();
        product.setId("product-1");
        product.setName("T-Shirt");
        product.setDescription("Cotton shirt");
        product.setPrice(new BigDecimal("49.90"));
        product.setImagePublicId("products/shirt");
        product.setCategory(category());
        product.setActive(true);
        ProductVariation variation = variation();
        variation.setProduct(product);
        product.setVariations(List.of(variation));
        return product;
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProductTests {
        @Test
        @DisplayName("Should save product with uploaded image and variation")
        void shouldCreateProductWhenCategoryExists() throws IOException {
            when(categoryRepository.findById("category-1")).thenReturn(Optional.of(category()));
            when(cloudinaryService.uploadPublic(any(), eq("products"))).thenReturn("products/shirt");

            CreateProductRequest request = new CreateProductRequest("T-Shirt", "Cotton shirt",
                    new BigDecimal("49.90"), "Blue", Size.M, 10, "category-1", image());
            productService.createProduct(request);

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(captor.capture());
            Product saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("T-Shirt");
            assertThat(saved.getDescription()).isEqualTo("Cotton shirt");
            assertThat(saved.getPrice()).isEqualByComparingTo("49.90");
            assertThat(saved.getImagePublicId()).isEqualTo("products/shirt");
            assertThat(saved.getCategory().getId()).isEqualTo("category-1");
            assertThat(saved.getVariations()).singleElement().satisfies(v -> {
                assertThat(v.getProduct()).isSameAs(saved);
                assertThat(v.getColor()).isEqualTo("Blue");
                assertThat(v.getSize()).isEqualTo(Size.M);
                assertThat(v.getStock()).isEqualTo(10);
            });
        }

        @Test
        @DisplayName("Should throw when category does not exist")
        void shouldThrowWhenCategoryDoesNotExist() throws IOException {
            when(categoryRepository.findById("missing")).thenReturn(Optional.empty());
            CreateProductRequest request = new CreateProductRequest("T-Shirt", null, BigDecimal.TEN,
                    "Blue", Size.M, 1, "missing", image());

            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(ResourceNotFoundException.class).hasMessage("Category not found");
            verify(cloudinaryService, never()).uploadPublic(any(), any());
            verify(productRepository, never()).save(any());
        }

        @Test
        void shouldRejectInvalidProductInputBeforeUploadingImage() {
            CreateProductRequest request = new CreateProductRequest("", null, new BigDecimal("-1"),
                    "", null, -1, "category-1", null);

            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Name is required");
            verifyNoInteractions(categoryRepository, cloudinaryService, productRepository);
        }
    }

    @Nested
    @DisplayName("product queries")
    class ProductQueryTests {
        @Test
        @DisplayName("Should list active products with image URLs")
        void shouldListActiveProducts() {
            Product product = product();
            when(productRepository.findByActiveTrue()).thenReturn(List.of(product));
            when(cloudinaryService.buildUrl("products/shirt")).thenReturn("https://image/shirt");

            List<ProductResponse> result = productService.listProducts();

            assertThat(result).singleElement().satisfies(response -> {
                assertThat(response.getId()).isEqualTo("product-1");
                assertThat(response.getName()).isEqualTo("T-Shirt");
                assertThat(response.getImageUrl()).isEqualTo("https://image/shirt");
                assertThat(response.getCategoryName()).isEqualTo("Shirts");
                assertThat(response.getVariations()).singleElement().satisfies(v -> {
                    assertThat(v.getColor()).isEqualTo("Blue");
                    assertThat(v.getSize()).isEqualTo("M");
                    assertThat(v.getStock()).isEqualTo(10);
                });
            });
        }

        @Test
        @DisplayName("Should return product by ID")
        void shouldFindProductByIdWhenProductExists() {
            when(productRepository.findById("product-1")).thenReturn(Optional.of(product()));
            when(cloudinaryService.buildUrl("products/shirt")).thenReturn("https://image/shirt");

            ProductResponse response = productService.findById("product-1");

            assertThat(response.getId()).isEqualTo("product-1");
            assertThat(response.getImageUrl()).isEqualTo("https://image/shirt");
        }

        @Test
        @DisplayName("Should throw when product ID does not exist")
        void shouldThrowWhenProductIdDoesNotExist() {
            when(productRepository.findById("missing")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> productService.findById("missing"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found with id: missing");
        }

        @Test
        @DisplayName("Should list products by category")
        void shouldListProductsByCategory() {
            when(productRepository.findByCategoryIdAndActiveTrue("category-1")).thenReturn(List.of(product()));
            when(cloudinaryService.buildUrl("products/shirt")).thenReturn("https://image/shirt");

            assertThat(productService.listProductsByCategory("category-1"))
                    .singleElement().extracting(ProductResponse::getId).isEqualTo("product-1");
        }

        @Test
        @DisplayName("Should search products by name")
        void shouldSearchProductsByName() {
            when(productRepository.findByNameContainingIgnoreCase("shirt")).thenReturn(List.of(product()));
            when(cloudinaryService.buildUrl("products/shirt")).thenReturn("https://image/shirt");

            assertThat(productService.searchByName("shirt"))
                    .singleElement().extracting(ProductResponse::getName).isEqualTo("T-Shirt");
        }

        @Test
        void shouldNotBuildImageUrlWhenProductHasNoImage() {
            Product product = product();
            product.setImagePublicId(null);
            when(productRepository.findByActiveTrue()).thenReturn(List.of(product));

            assertThat(productService.listProducts()).singleElement()
                    .extracting(ProductResponse::getImageUrl).isNull();
            verify(cloudinaryService, never()).buildUrl(any());
        }
    }

    @Nested
    @DisplayName("createVariation")
    class CreateVariationTests {
        @Test
        @DisplayName("Should save variation linked to product")
        void shouldCreateVariationWhenProductExists() {
            Product product = product();
            when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

            productService.createVariation("product-1", "Red", Size.G, 4);

            ArgumentCaptor<ProductVariation> captor = ArgumentCaptor.forClass(ProductVariation.class);
            verify(productVariationRepository).save(captor.capture());
            assertThat(captor.getValue().getProduct()).isSameAs(product);
            assertThat(captor.getValue().getColor()).isEqualTo("Red");
            assertThat(captor.getValue().getSize()).isEqualTo(Size.G);
            assertThat(captor.getValue().getStock()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should throw when creating variation for missing product")
        void shouldThrowWhenCreatingVariationForMissingProduct() {
            when(productRepository.findById("missing")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> productService.createVariation("missing", "Red", Size.G, 4))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Product not found");
            verify(productVariationRepository, never()).save(any());
        }

        @Test
        void shouldRejectDuplicateOrInvalidVariation() {
            Product product = product();
            when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> productService.createVariation("product-1", "blue", Size.M, 4))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Variation already exists");
            assertThatThrownBy(() -> productService.createVariation("product-1", "Red", Size.G, -1))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Stock must be non-negative");
            verify(productVariationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProductTests {
        @Test
        @DisplayName("Should update product fields variation and replace existing image")
        void shouldUpdateProductWhenFieldsAndImageAreProvided() throws IOException {
            Product product = product();
            when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
            when(cloudinaryService.uploadPublic(any(), eq("products"))).thenReturn("products/new-shirt");

            UpdateProductRequest request = new UpdateProductRequest("New shirt", "New description",
                    new BigDecimal("59.90"), "Green", Size.GG, 20, null, image(), false);
            productService.updateProduct("product-1", request);

            assertThat(product.getName()).isEqualTo("New shirt");
            assertThat(product.getDescription()).isEqualTo("New description");
            assertThat(product.getPrice()).isEqualByComparingTo("59.90");
            assertThat(product.getActive()).isFalse();
            assertThat(product.getImagePublicId()).isEqualTo("products/new-shirt");
            assertThat(product.getVariations().get(0).getColor()).isEqualTo("Green");
            assertThat(product.getVariations().get(0).getSize()).isEqualTo(Size.GG);
            assertThat(product.getVariations().get(0).getStock()).isEqualTo(20);
            verify(cloudinaryService).delete("products/shirt");
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should preserve fields when update values are null and image is empty")
        void shouldPreserveProductWhenUpdateValuesAreAbsent() throws IOException {
            Product product = product();
            when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
            UpdateProductRequest request = new UpdateProductRequest(null, null, null, null, null,
                    null, null, new MockMultipartFile("image", new byte[0]), null);

            productService.updateProduct("product-1", request);

            assertThat(product.getName()).isEqualTo("T-Shirt");
            assertThat(product.getPrice()).isEqualByComparingTo("49.90");
            assertThat(product.getImagePublicId()).isEqualTo("products/shirt");
            verify(productRepository).save(product);
            verify(cloudinaryService, never()).delete(any());
            verify(cloudinaryService, never()).uploadPublic(any(), any());
        }

        @Test
        @DisplayName("Should throw when updating missing product")
        void shouldThrowWhenUpdatingMissingProduct() throws IOException {
            when(productRepository.findById("missing")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> productService.updateProduct("missing", new UpdateProductRequest(
                    null, null, null, null, null, null, null, null, null)))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(productRepository, never()).save(any());
        }

        @Test
        void shouldPreserveOldImageWhenNewUploadFails() throws IOException {
            Product product = product();
            when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
            when(cloudinaryService.uploadPublic(any(), eq("products")))
                    .thenThrow(new IOException("upload failed"));

            UpdateProductRequest request = new UpdateProductRequest(null, null, null, null, null,
                    null, null, image(), null);

            assertThatThrownBy(() -> productService.updateProduct("product-1", request))
                    .isInstanceOf(IOException.class);
            assertThat(product.getImagePublicId()).isEqualTo("products/shirt");
            verify(cloudinaryService, never()).delete(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        void shouldRejectVariationUpdateWhenProductHasNoVariations() throws IOException {
            Product product = product();
            product.setVariations(List.of());
            when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

            UpdateProductRequest request = new UpdateProductRequest(null, null, null, "Red", null,
                    null, null, null, null);

            assertThatThrownBy(() -> productService.updateProduct("product-1", request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Product has no variation to update");
            verify(productRepository, never()).save(any());
        }
    }
}