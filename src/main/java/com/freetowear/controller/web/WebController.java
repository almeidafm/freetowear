package com.freetowear.controller.web;

import com.freetowear.dto.request.account.RegisterRequest;
import com.freetowear.dto.response.category.CategoryResponse;
import com.freetowear.dto.response.product.ProductResponse;
import com.freetowear.infra.security.CustomerDetails;
import com.freetowear.service.CategoryService;
import com.freetowear.service.OrderService;
import com.freetowear.service.ProductService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class WebController {

    private final ProductService productService;
    private final OrderService orderService;
    private final CategoryService categoryService;

    public WebController(
            ProductService productService,
            OrderService orderService,
            CategoryService categoryService
    ) {
        this.productService = productService;
        this.orderService = orderService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home() { return "index"; }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("request", new RegisterRequest());
        return "register";
    }

    @GetMapping("/login")
    public String login(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            Model model,
            HttpSession session
    ) {
        Boolean emailVerificationPopupShown =
                (Boolean) session.getAttribute("emailVerificationPopupShown");

        if (customerDetails != null
                && !customerDetails.isEmailVerified()
                && !Boolean.TRUE.equals(emailVerificationPopupShown)) {

            model.addAttribute("showEmailVerificationPopup", true);

            session.setAttribute("emailVerificationPopupShown", true);
        }

        return "login";
    }

    @GetMapping("/account")
    public String account() { return "account"; }

    @GetMapping("/cart")
    public String cart(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            Model model
    ) {
        if (customerDetails != null) {
            model.addAttribute("cart", orderService.getCart(customerDetails.getId()).orElse(null));
        } else {
            model.addAttribute("cart", null);
        }
        return "cart";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }

    @GetMapping("/products/search")
    public String search(
            @RequestParam(required = false) String q,
            Model model) {

        String query = q == null ? "" : q.trim();

        List<ProductResponse> products = query.isBlank() ? List.of() : productService.searchByName(query);

        model.addAttribute("products", products);
        model.addAttribute("query", query);

        return "search";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categories";
    }

    @GetMapping("/categories/{id}")
    public String category(
            @PathVariable String id,
            Model model
    ) {
        CategoryResponse category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria não encontrada"
                ));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Categoria não encontrada"
            );
        }

        model.addAttribute("category", category);
        model.addAttribute(
                "products",
                productService.listProductsByCategory(id)
        );

        return "category";
    }

    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("products", productService.listProducts());
        return "test";
    }
}