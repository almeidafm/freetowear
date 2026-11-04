package com.freetowear.service;

import com.freetowear.entity.Category;
import com.freetowear.repository.CategoryRepository;
import com.freetowear.dto.request.category.CreateCategoryRequest;
import com.freetowear.dto.request.category.UpdateCategoryRequest;
import com.freetowear.dto.response.category.CategoryResponse;
import com.freetowear.exception.ResourceNotFoundException;

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
        String name = validateName(request == null ? null : request.getName());
        if (categoryRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new IllegalArgumentException("Category name already in use");
        }
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
    }

    public void updateCategory(String id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        if (request == null) throw new IllegalArgumentException("Category request is required");
        if (request.getName() != null) {
            String name = validateName(request.getName());
            categoryRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
                if (!id.equals(existing.getId())) throw new IllegalArgumentException("Category name already in use");
            });
            category.setName(name);
        }
        if (request.getActive() != null) category.setActive(request.getActive());
        categoryRepository.save(category);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }

    public Optional<CategoryResponse> getCategoryById(String id) {
        return categoryRepository.findById(id)
                .map(CategoryResponse::new);
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name is required");
        String normalized = name.trim();
        if (normalized.length() > 100) throw new IllegalArgumentException("Category name cannot exceed 100 characters");
        return normalized;
    }
}