package com.freetowear.freetowear.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryRequest {

    @NotBlank
    private String name;

    public CreateCategoryRequest() {}

    public CreateCategoryRequest(String name) {
        this.name = name;
    }
}