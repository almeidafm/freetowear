package com.freetowear.freetowear.controller.api.admin;

import com.freetowear.freetowear.entity.Category;
import com.freetowear.freetowear.entity.Product;
import com.freetowear.freetowear.entity.ProductVariation;
import com.freetowear.freetowear.repository.CategoryRepository;
import com.freetowear.freetowear.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/*
 * ProductController — manages products. (admin only)
 * POST   /product/create
 * GET    /product
 * GET    /product/{id}
 * PATCH  /product/{id}
 * */
@Controller
@RequestMapping("/product")
public class ProductController {

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping("/create")
    public String createProduct(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam String color,
            @RequestParam ProductVariation.Size size,
            @RequestParam Integer stock,
            @RequestParam Integer categoryId,
            @RequestParam("image") MultipartFile image
    ) {
        try {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);

            Files.createDirectories(path.getParent());
            Files.write(path, image.getBytes());

            String imagePath = "uploads/" + fileName;

            Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
            if (categoryOpt.isEmpty()) {
                return "redirect:/error";
            }

            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setImageUrl(imagePath);
            product.setCategory(categoryOpt.get());

            ProductVariation variation = new ProductVariation();
            variation.setColor(color);
            variation.setSize(size);
            variation.setStock(stock);
            variation.setProduct(product);

            product.setVariations(List.of(variation));

            productRepository.save(product);

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/error";
        }

        return "redirect:/";
    }

    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productRepository.findByActiveTrue();
        model.addAttribute("products", products);
        return "products";
    }

    @GetMapping("/{id}")
    public String showProduct(@PathVariable Integer id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        return "product";
    }
}