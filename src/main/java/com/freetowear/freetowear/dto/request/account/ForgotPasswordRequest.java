package com.freetowear.freetowear.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ForgotPasswordRequest {

    @NotBlank(message = "Contact is required")
    private String contact;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "New password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must have 8+ characters, upper/lowercase, number, and special character"
    )
    private String newPassword;

    public ForgotPasswordRequest() {}

    public ForgotPasswordRequest(String contact, String code, String newPassword) {
        this.contact = contact;
        this.code = code;
        this.newPassword = newPassword;
    }

    // getters and setters
    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}