package com.freetowear.freetowear.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteAccountRequest {

    @NotBlank(message = "Password is required")
    private String password;

    public DeleteAccountRequest() {}

    public DeleteAccountRequest(String password) {
        this.password = password;
    }
}