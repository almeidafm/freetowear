package com.freetowear.freetowear.dto.response.category;

import com.freetowear.freetowear.entity.Category;

public class CategoryResponse {

    private Integer id;
    private String name;
    private Boolean active;

    public CategoryResponse() {}

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.active = category.getActive();
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getActive() {
        return active;
    }
}