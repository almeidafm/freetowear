package com.freetowear.freetowear.dto.request.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeEmailRequest {

    @NotBlank(message = "Current email is required")
    @Email(message = "Invalid email format")
    private String currentEmail;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "New email is required")
    @Email(message = "Invalid email format")
    private String newEmail;

    public ChangeEmailRequest() {}

    public ChangeEmailRequest(String currentEmail, String password, String newEmail) {
        this.currentEmail = currentEmail;
        this.password = password;
        this.newEmail = newEmail;
    }
}