package com.freetowear.freetowear.dto.response.account;

import com.freetowear.freetowear.entity.Customer;
import java.time.LocalDate;

public class CustomerResponse {

    private Integer id;
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

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Boolean getActive() {
        return active;
    }
}