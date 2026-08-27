package com.freetowear.controller.api.admin;

import com.freetowear.dto.request.category.CreateCategoryRequest;
import com.freetowear.dto.request.category.UpdateCategoryRequest;
import com.freetowear.dto.response.category.CategoryResponse;
import com.freetowear.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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
            @PathVariable String id,
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
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable String id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}