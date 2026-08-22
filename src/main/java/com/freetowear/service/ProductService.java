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
import com.freetowear.infra.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
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
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

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
        System.out.println("Products found: " + products.size());
        return products.stream()
                .map(product -> new ProductResponse(product, cloudinaryService.buildUrl(product.getImagePublicId())))
                .collect(Collectors.toList());
    }

    public ProductResponse findById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        String imageUrl = cloudinaryService.buildUrl(product.getImagePublicId());
        return new ProductResponse(product, imageUrl);
    }

    public void createVariation(String productId, String color, Size size, Integer stock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        ProductVariation variation = new ProductVariation();
        variation.setProduct(product);
        variation.setColor(color);
        variation.setSize(size);
        variation.setStock(stock);

        productVariationRepository.save(variation);
    }

    public void updateProduct(String id, UpdateProductRequest request) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getActive() != null) product.setActive(request.getActive());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            if (product.getImagePublicId() != null) {
                cloudinaryService.delete(product.getImagePublicId());
            }
            String publicId = cloudinaryService.uploadPublic(request.getImage(), "products");
            product.setImagePublicId(publicId);
        }

        if (request.getColor() != null || request.getSize() != null || request.getStock() != null) {
            ProductVariation variation = product.getVariations().get(0);
            if (request.getColor() != null) variation.setColor(request.getColor());
            if (request.getSize() != null) variation.setSize(request.getSize());
            if (request.getStock() != null) variation.setStock(request.getStock());
        }

        productRepository.save(product);
    }
}
