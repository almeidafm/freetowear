package com.freetowear.freetowear.controller.api.customer;

import com.freetowear.freetowear.entity.Customer;
import com.freetowear.freetowear.entity.Address;
import com.freetowear.freetowear.repository.CustomerRepository;
import com.freetowear.freetowear.repository.AddressRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/*
 * AccountController — manages customer account operations.
 * POST   /account/register ✔
 * POST   /account/{id}/address ✔
 * GET    /account/{id} ⏳
 * PATCH  /account/{id} ✔
 * PATCH  /account/{id}/email ⏳
 * PATCH  /account/{id}/password ⏳
 * PATCH  /account/password/reset ⏳
 * DELETE  /account/{id} ⏳
 * */
@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private CustomerRepository accountRepository;

    @Autowired
    private AddressRepository addressRepository;

    @PostMapping("/register")
    public String register(@Valid Customer customer, BindingResult result) {
        if (result.hasErrors()) return "redirect:/register";
        accountRepository.save(customer);
        return "redirect:/";
    }

    @DeleteMapping("/{id}")
    public String deleteAccount(@PathVariable Integer id) {
        accountRepository.findById(id).ifPresent(customer-> {
            customer.setActive(false);
            accountRepository.save(customer);
        });
        return "redirect:/";
    }

    @PatchMapping("/{id}")
    public String updateAccount(
            @PathVariable Integer id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String birthDate,
            @RequestParam(required = false) String phone
    ) {
        accountRepository.findById(id).ifPresent(customer -> {
            if (name != null && !name.isEmpty()) customer.setName(name);
            if (cpf != null && !cpf.isEmpty()) customer.setCpf(cpf);
            if (birthDate != null && !birthDate.isEmpty())
                customer.setBirthDate(LocalDate.parse(birthDate));
            if (phone != null && !phone.isEmpty()) customer.setPhone(phone);
            accountRepository.save(customer);
        });
        return "redirect:/account/" + id;
    }

    @PostMapping("/{id}/address")
    public String addAddress(
            @PathVariable Integer id,
            @RequestParam String cep,
            @RequestParam String street,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String complement,
            @RequestParam String neighborhood,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam(required = false, defaultValue = "false") Boolean defaultAddress
    ) {
        accountRepository.findById(id).ifPresent(customer -> {
            Address address = new Address();
            address.setCustomer(customer);
            address.setCep(cep);
            address.setStreet(street);
            address.setNumber(number);
            address.setComplement(complement);
            address.setNeighborhood(neighborhood);
            address.setCity(city);
            address.setState(state);
            address.setDefaultAddress(defaultAddress);
            addressRepository.save(address);
        });
        return "redirect:/account/" + id;
    }
}