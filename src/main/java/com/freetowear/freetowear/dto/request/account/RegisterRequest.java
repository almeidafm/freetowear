package com.freetowear.freetowear.dto.request.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    @Pattern(
            regexp = "^[\\p{L} '-]+$",
            message = "Name must contain only letters"
    )
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must have 8+ characters, upper/lowercase, number, and special character"
    )
    private String password;

    @Pattern(
            regexp = "^\\d{10,11}$",
            message = "CPF must be 10 or 11 digits"
    )
    private String cpf;

    private String birthDate;

    @Pattern(
            regexp = "^\\d{10,11}$",
            message = "Phone must be 10 or 11 digits"
    )
    private String phone;

    public RegisterRequest(String name, String email, String password, String cpf, String birthDate, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.cpf = cpf;
        this.birthDate = birthDate;
        this.phone = phone;
    }
}