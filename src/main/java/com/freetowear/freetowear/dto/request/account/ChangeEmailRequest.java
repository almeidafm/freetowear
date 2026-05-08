package com.freetowear.freetowear.dto.request.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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

    // getters and setters
    public String getCurrentEmail() {
        return currentEmail;
    }

    public void setCurrentEmail(String currentEmail) {
        this.currentEmail = currentEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }
}