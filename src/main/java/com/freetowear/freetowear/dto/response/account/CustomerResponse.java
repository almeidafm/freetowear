package com.freetowear.freetowear.dto.response.account;

import com.freetowear.freetowear.entity.Customer;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class CustomerResponse {

    private String id;
    private String name;
    private String email;
    private String phone;
    private String cpf;
    private LocalDate birthDate;
    private Boolean active;

    public CustomerResponse() {}

    public CustomerResponse(Customer customer) {
        this.id = customer.getId();
        this.name = customer.getName();
        this.email = customer.getEmail();
        this.phone = customer.getPhone();
        this.cpf = customer.getCpf();
        this.birthDate = customer.getBirthDate();
        this.active = customer.getActive();
    }
}