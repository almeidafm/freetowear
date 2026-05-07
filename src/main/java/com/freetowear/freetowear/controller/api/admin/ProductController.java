package com.freetowear.freetowear.controller.api.admin;

import com.freetowear.freetowear.dto.request.product.CreateProductRequest;
import com.freetowear.freetowear.enums.Size;
import com.freetowear.freetowear.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;

/*
 * ProductController — manages products. (admin only)
 * POST   /product/create ✔
 * GET    /product ✔
 * GET    /product/{id} ✔
 * PATCH  /product/{id} ⏳
 * */
@Controller
@RequestMapping("/product")
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
            @RequestParam Integer categoryId,
            @RequestParam("image") MultipartFile image
    ) {
        try {
            productService.createProduct(new CreateProductRequest(
                    name, description, price, color, size, stock, categoryId, image
            ));
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/error";
        }
        return "redirect:/";
    }

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.listProducts());
        return "products";
    }

    @GetMapping("/{id}")
    public String showProduct(@PathVariable Integer id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "product";
    }
}