package com.freetowear.freetowear.service;

import com.freetowear.freetowear.dto.request.product.CreateProductRequest;
import com.freetowear.freetowear.dto.response.product.ProductResponse;
import com.freetowear.freetowear.entity.Category;
import com.freetowear.freetowear.entity.Product;
import com.freetowear.freetowear.entity.ProductVariation;
import com.freetowear.freetowear.repository.CategoryRepository;
import com.freetowear.freetowear.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public void createProduct(CreateProductRequest request) throws IOException {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        String imagePath = saveImage(request.getImage());

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(imagePath);
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
        return productRepository.findByActiveTrue()
                .stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
    }

    public ProductResponse findById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return new ProductResponse(product);
    }

    private String saveImage(MultipartFile image) throws IOException {
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, image.getBytes());
        return "uploads/" + fileName;
    }
}