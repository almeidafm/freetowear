package com.freetowear.freetowear.dto.request.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {

    private String name;
    private Boolean active;

    public UpdateCategoryRequest() {}

    public UpdateCategoryRequest(String name, Boolean active) {
        this.name = name;
        this.active = active;
    }
}