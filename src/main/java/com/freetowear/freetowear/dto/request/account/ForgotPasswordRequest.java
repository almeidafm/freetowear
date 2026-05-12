package com.freetowear.freetowear.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}