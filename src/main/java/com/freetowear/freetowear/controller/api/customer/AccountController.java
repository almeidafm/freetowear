package com.freetowear.freetowear.controller.api.customer;

import com.freetowear.freetowear.dto.request.account.AddAddressRequest;
import com.freetowear.freetowear.dto.request.account.RegisterRequest;
import com.freetowear.freetowear.dto.request.account.UpdateAccountRequest;
import com.freetowear.freetowear.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/*
 * AccountController — manages customer account operations.
 * POST   /account/register ✔
 * POST   /account/{id}/address ✔
 * GET    /account/{id} ⏳
 * PATCH  /account/{id} ✔
 * PATCH  /account/{id}/email ⏳
 * PATCH  /account/{id}/password ⏳
 * PATCH  /account/password/reset ⏳
 * DELETE /account/{id} ⏳
 * */
@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/register")
    public String register(@Valid RegisterRequest request, BindingResult result) {
        if (result.hasErrors()) return "redirect:/";
        accountService.register(request);
        return "redirect:/";
    }

    @DeleteMapping("/{id}")
    public String deleteAccount(@PathVariable Integer id) {
        accountService.deleteAccount(id);
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
        accountService.updateAccount(id, new UpdateAccountRequest(name, cpf, birthDate, phone));
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
        accountService.addAddress(id, new AddAddressRequest(
                cep, street, number, complement,
                neighborhood, city, state, defaultAddress
        ));
        return "redirect:/account/" + id;
    }
}