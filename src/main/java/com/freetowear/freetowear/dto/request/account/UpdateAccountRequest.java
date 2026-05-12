package com.freetowear.freetowear.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAccountRequest {

    @NotBlank
    @Pattern(
            regexp = "^[\\p{L} '-]+$",
            message = "Name must contain only letters"
    )
    private String name;

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

    public UpdateAccountRequest() {}

    public UpdateAccountRequest(String name, String cpf, String birthDate, String phone) {
        this.name = name;
        this.cpf = cpf;
        this.birthDate = birthDate;
        this.phone = phone;
    }
}