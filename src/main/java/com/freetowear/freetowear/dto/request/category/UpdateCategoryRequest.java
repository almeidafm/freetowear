package com.freetowear.freetowear.dto.request.category;

public class UpdateCategoryRequest {

    private String name;
    private Boolean active;

    public UpdateCategoryRequest() {}

    public UpdateCategoryRequest(String name, Boolean active) {
        this.name = name;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}