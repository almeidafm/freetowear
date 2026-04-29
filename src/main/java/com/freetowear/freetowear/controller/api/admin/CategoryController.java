package com.freetowear.freetowear.controller.api.admin;

import com.freetowear.freetowear.entity.Category;
import com.freetowear.freetowear.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/*
 * CategoryController — manages product categories.
 * POST   /category/create ✔
 * GET    /category ⏳
 * GET    /category/{id} ⏳
 * PATCH  /category/{id} ✔
 * */
@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping("/create")
    public String createCategory(@RequestParam String name) {
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);

        return "redirect:/";
    }

    @PatchMapping("/{id}")
    public String updateCategory(
            @PathVariable Integer id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active
    ) {
        categoryRepository.findById(id).ifPresent(category -> {
            if (name != null && !name.isEmpty()) category.setName(name);
            if (active != null) category.setActive(active);

            categoryRepository.save(category);
        });

        return "redirect:/";
    }
}
