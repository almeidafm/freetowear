package com.freetowear.freetowear.controller.api.admin;

import com.freetowear.freetowear.dto.request.category.CreateCategoryRequest;
import com.freetowear.freetowear.dto.request.category.UpdateCategoryRequest;
import com.freetowear.freetowear.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.freetowear.freetowear.dto.response.category.CategoryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;

/*
 * CategoryController — manages product categories.
 * POST   /category/create ✔
 * GET    /category ✔
 * GET    /category/{id} ✔
 * PATCH  /category/{id} ✔
 * */
@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/create")
    public String createCategory(@RequestParam String name) {
        categoryService.createCategory(new CreateCategoryRequest(name));
        return "redirect:/";
    }

    @PatchMapping("/{id}")
    public String updateCategory(
            @PathVariable Integer id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active
    ) {
        categoryService.updateCategory(id, new UpdateCategoryRequest(name, active));
        return "redirect:/";
    }

    @GetMapping
    @ResponseBody
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Integer id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}