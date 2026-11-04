package com.freetowear.service;

import com.freetowear.entity.Category;
import com.freetowear.entity.Product;
import com.freetowear.entity.ProductVariation;
import com.freetowear.enums.Size;
import com.freetowear.repository.CategoryRepository;
import com.freetowear.repository.ProductRepository;
import com.freetowear.repository.ProductVariationRepository;
import com.freetowear.dto.request.product.CreateProductRequest;
import com.freetowear.dto.request.product.UpdateProductRequest;
import com.freetowear.dto.response.product.ProductResponse;
import com.freetowear.exception.ResourceNotFoundException;
import com.freetowear.infra.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariationRepository productVariationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public void createProduct(CreateProductRequest request) throws IOException {
        validateProductInput(request);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String publicId = cloudinaryService.uploadPublic(request.getImage(), "products"); // 👈

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImagePublicId(publicId);
        product.setCategory(category);

        ProductVariation variation = new ProductVariation();
        variation.setColor(request.getColor());
        variation.setSize(request.getSize());
        variation.setStock(request.getStock());
        variation.setProduct(product);

        product.setVariations(List.of(variation));
        productRepository.save(product);
    }

    public List<ProductResponse> listProducts() {
        List<Product> products = productRepository.findByActiveTrue();
        return products.stream()
                .map(product -> new ProductResponse(product, imageUrl(product.getImagePublicId())))
                .collect(Collectors.toList());
    }

    public ProductResponse findById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return new ProductResponse(product, imageUrl(product.getImagePublicId()));
    }

    public void createVariation(String productId, String color, Size size, Integer stock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        validateVariationInput(color, size, stock);
        if (!Boolean.TRUE.equals(product.getActive())) throw new IllegalArgumentException("Product is not active");
        if (product.getVariations() != null && product.getVariations().stream()
                .anyMatch(v -> color.equalsIgnoreCase(v.getColor()) && size == v.getSize())) {
            throw new IllegalArgumentException("Variation already exists");
        }

        ProductVariation variation = new ProductVariation();
        variation.setProduct(product);
        variation.setColor(color);
        variation.setSize(size);
        variation.setStock(stock);

        productVariationRepository.save(variation);
    }

    public void updateProduct(String id, UpdateProductRequest request) throws IOException {
        if (request == null) throw new IllegalArgumentException("Product update request is required");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price cannot be negative");
            product.setPrice(request.getPrice());
        }
        if (request.getActive() != null) product.setActive(request.getActive());

        boolean hasNewImage = request.getImage() != null && !request.getImage().isEmpty();
        if (hasNewImage) {
            boolean hasExistingImage = product.getImagePublicId() != null;
            String publicId = cloudinaryService.uploadPublic(request.getImage(), "products");
            if (hasExistingImage) cloudinaryService.delete(product.getImagePublicId());
            product.setImagePublicId(publicId);
        }

        boolean hasVariationUpdates = request.getColor() != null
                || request.getSize() != null
                || request.getStock() != null;

        if (hasVariationUpdates) {
            ProductVariation variation = product.getVariations() == null ? null
                    : product.getVariations().stream().findFirst().orElse(null);
            if (variation == null) throw new IllegalArgumentException("Product has no variation to update");
            if (request.getStock() != null && request.getStock() < 0) throw new IllegalArgumentException("Stock cannot be negative");
            if (request.getColor() != null && request.getColor().isBlank()) throw new IllegalArgumentException("Color is required");
            if (request.getColor() != null) variation.setColor(request.getColor());
            if (request.getSize() != null) variation.setSize(request.getSize());
            if (request.getStock() != null) variation.setStock(request.getStock());
        }

        productRepository.save(product);
    }

    public List<ProductResponse> listProductsByCategory(String categoryId) {
        List<Product> products =
                productRepository.findByCategoryIdAndActiveTrue(categoryId);

        return products.stream()
                .map(product ->
                        new ProductResponse(
                                product,
                                imageUrl(product.getImagePublicId())
                        )
                )
                .collect(Collectors.toList());
    }

    public List<ProductResponse> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(product -> new ProductResponse(
                        product,
                        imageUrl(product.getImagePublicId())
                ))
                .toList();
    }

    private void validateProductInput(CreateProductRequest request) {
        if (request == null) throw new IllegalArgumentException("Product request is required");
        if (request.getName() == null || request.getName().isBlank()) throw new IllegalArgumentException("Name is required");
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price must be non-negative");
        if (request.getImage() == null || request.getImage().isEmpty()) throw new IllegalArgumentException("Image is required");
        if (request.getCategoryId() == null || request.getCategoryId().isBlank()) throw new IllegalArgumentException("Category is required");
        validateVariationInput(request.getColor(), request.getSize(), request.getStock());
    }

    private void validateVariationInput(String color, Size size, Integer stock) {
        if (color == null || color.isBlank()) throw new IllegalArgumentException("Color is required");
        if (size == null) throw new IllegalArgumentException("Size is required");
        if (stock == null || stock < 0) throw new IllegalArgumentException("Stock must be non-negative");
    }

    private String imageUrl(String imagePublicId) {
        return imagePublicId == null || imagePublicId.isBlank()
                ? null : cloudinaryService.buildUrl(imagePublicId);
    }
}