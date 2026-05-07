package com.freetowear.freetowear.service;

import com.freetowear.freetowear.dto.request.category.CreateCategoryRequest;
import com.freetowear.freetowear.dto.request.category.UpdateCategoryRequest;
import com.freetowear.freetowear.dto.response.category.CategoryResponse;
import com.freetowear.freetowear.entity.Category;
import com.freetowear.freetowear.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public void createCategory(CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        categoryRepository.save(category);
    }

    public void updateCategory(Integer id, UpdateCategoryRequest request) {
        categoryRepository.findById(id).ifPresent(category -> {
            if (request.getName() != null && !request.getName().isEmpty())
                category.setName(request.getName());
            if (request.getActive() != null)
                category.setActive(request.getActive());
            categoryRepository.save(category);
        });
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }

    public Optional<CategoryResponse> getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .map(CategoryResponse::new);
    }
}