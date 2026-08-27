package com.freetowear.controller.api.admin;

import com.freetowear.dto.request.product.CreateProductRequest;
import com.freetowear.dto.request.product.UpdateProductRequest;
import com.freetowear.service.ProductService;
import com.freetowear.enums.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import java.io.IOException;
import java.math.BigDecimal;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/create")
    public String createProduct(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam String color,
            @RequestParam Size size,
            @RequestParam Integer stock,
            @RequestParam String categoryId,
            @RequestParam("image") MultipartFile image,
            Model model
    ) {
        try {
            productService.createProduct(new CreateProductRequest(
                    name, description, price, color, size, stock, categoryId, image
            ));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "product-form";
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload image, try again.");
            return "product-form";
        }
        return "redirect:/";
    }

    @PostMapping("/variation/create")
    public String createVariation(
            @RequestParam String productId,
            @RequestParam String color,
            @RequestParam Size size,
            @RequestParam Integer stock
    ) {
        productService.createVariation(productId, color, size, stock);
        return "redirect:/test";
    }

    @PatchMapping("/{id}")
    public String updateProduct(
            @PathVariable String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Size size,
            @RequestParam(required = false) Integer stock,
            @RequestParam(required = false) String categoryId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(required = false) Boolean active,
            Model model
    ) {
        try {
            productService.updateProduct(id, new UpdateProductRequest(
                    name, description, price, color, size, stock, categoryId, image, active
            ));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "product-form";
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload image, try again.");
            return "product-form";
        }
        return "redirect:/product/" + id;
    }

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.listProducts());
        return "products";
    }

    @GetMapping("/{id}")
    public String showProduct(@PathVariable String id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "product";
    }
}