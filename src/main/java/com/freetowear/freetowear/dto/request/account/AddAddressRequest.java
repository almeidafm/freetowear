package com.freetowear.freetowear.dto.request.account;

import com.freetowear.freetowear.enums.UF;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAddressRequest {

    @NotBlank
    @Pattern(regexp = "^\\d{8}$", message = "CEP must be 8 digits")
    private String cep;

    @NotBlank
    @Size(min = 2, max = 100)
    private String street;

    @Size(max = 10)
    private String number;

    @Size(max = 50)
    private String complement;

    @NotBlank
    @Size(min = 2, max = 100)
    private String neighborhood;

    @NotBlank
    @Size(min = 2, max = 100)
    private String city;

    private UF state;

    private Boolean defaultAddress = false;

    public AddAddressRequest() {}

    public AddAddressRequest(
            String cep,
            String street,
            String number,
            String complement,
            String neighborhood,
            String city,
            UF state,
            Boolean defaultAddress
    ) {
        this.cep = cep;
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.defaultAddress = defaultAddress != null ? defaultAddress : false;
    }
}