package com.freetowear.freetowear.dto.request.account;

public class UpdateAccountRequest {

    private String name;
    private String cpf;
    private String birthDate;
    private String phone;

    public UpdateAccountRequest() {}

    public UpdateAccountRequest(String name, String cpf, String birthDate, String phone) {
        this.name = name;
        this.cpf = cpf;
        this.birthDate = birthDate;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}