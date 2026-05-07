package com.freetowear.freetowear.dto.request.account;

import com.freetowear.freetowear.enums.UF;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public UF getState() {
        return state;
    }

    public void setState(UF state) {
        this.state = state;
    }

    public Boolean getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(Boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }
}