package com.freetowear.freetowear.dto.response.account;

import com.freetowear.freetowear.entity.Address;

public class AddressResponse {

    private Integer id;
    private String cep;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private Boolean defaultAddress;

    public AddressResponse() {}

    public AddressResponse(Address address) {
        this.id = address.getId();
        this.cep = address.getCep();
        this.street = address.getStreet();
        this.number = address.getNumber();
        this.complement = address.getComplement();
        this.neighborhood = address.getNeighborhood();
        this.city = address.getCity();
        this.state = address.getState();
        this.defaultAddress = address.getDefaultAddress();
    }

    public Integer getId() {
        return id;
    }

    public String getCep() {
        return cep;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public Boolean getDefaultAddress() {
        return defaultAddress;
    }
}