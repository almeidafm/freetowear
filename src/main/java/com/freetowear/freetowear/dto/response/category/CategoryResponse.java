package com.freetowear.freetowear.dto.response.category;

import com.freetowear.freetowear.entity.Category;
import lombok.Getter;

@Getter
public class CategoryResponse {

    private String id;
    private String name;
    private Boolean active;

    public CategoryResponse() {}

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.active = category.getActive();
    }
}