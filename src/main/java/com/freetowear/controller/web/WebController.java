package com.freetowear.controller.web;

import com.freetowear.dto.request.account.RegisterRequest;
import com.freetowear.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * WebController — maps GET routes to Thymeleaf templates. No business logic.
 * */
@Controller
public class WebController {

    private final ProductService productService;

    public WebController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("request", new RegisterRequest());
        return "register";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/account")
    public String account() {
        return "account";
    }

    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }

    @GetMapping("/products/search")
    public String search() {
        return "search";
    }

    @GetMapping("/categories")
    public  String categories() {
        return "categories";
    }

    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("products", productService.listProducts());
        return "test";
    }
}