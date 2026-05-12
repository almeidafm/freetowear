package com.freetowear.freetowear.dto.response.account;

import com.freetowear.freetowear.entity.Address;
import com.freetowear.freetowear.enums.UF;
import lombok.Getter;

@Getter
public class AddressResponse {

    private String id;
    private String cep;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private UF state;
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
}