package com.freetowear.controller.api.customer;

import com.freetowear.dto.request.account.*;
import com.freetowear.dto.request.account.*;
import com.freetowear.dto.response.account.AddressResponse;
import com.freetowear.dto.response.account.CustomerResponse;
import com.freetowear.infra.security.CustomerDetails;
import com.freetowear.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * AccountController — manages customer account operations.
 * POST   /account/register ✔
 * POST   /account/{id}/address ✔
 * GET    /account/{id} ✔
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

    @GetMapping("/{id}")
    public String getAccount(@PathVariable String id, Model model) {
        CustomerResponse customer = accountService.getAccount(id);
        model.addAttribute("customer", customer);
        return "account";
    }

    @PostMapping("/register")
    public String register(@Valid RegisterRequest request, BindingResult result) {
        if (result.hasErrors()) return "redirect:/";
        accountService.register(request);
        return "redirect:/";
    }

    @PatchMapping
    public String updateAccount(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String birthDate,
            @RequestParam(required = false) String phone
    ) {
        String id = customerDetails.getId();
        accountService.updateAccount(id, new UpdateAccountRequest(name, cpf, birthDate, phone));
        return "redirect:/account/" + id;
    }

    @PostMapping("/address")
    public String addAddress(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @Valid @ModelAttribute AddAddressRequest request
    ) {
        accountService.addAddress(customerDetails.getId(), request);
        return "redirect:/";
    }

    @GetMapping("/address")
    @ResponseBody
    public List<AddressResponse> getAddresses(
            @AuthenticationPrincipal CustomerDetails customerDetails
    ) {
        return accountService.getAddresses(customerDetails.getId());
    }

    @PatchMapping("/{id}/email")
    @ResponseBody
    public ResponseEntity<String> changeEmail(
            @PathVariable String id,
            @Valid @ModelAttribute ChangeEmailRequest request,
            BindingResult result
    ) {
        if (result.hasErrors()) return ResponseEntity.badRequest().body("Validation error");
        accountService.changeEmail(id, request);
        return ResponseEntity.ok("Email changed successfully");
    }

    @PatchMapping("/{id}/password")
    @ResponseBody
    public ResponseEntity<String> changePassword(
            @PathVariable String id,
            @Valid @ModelAttribute ChangePasswordRequest request,
            BindingResult result
    ) {
        if (result.hasErrors()) return ResponseEntity.badRequest().body("Validation error");
        accountService.changePassword(id, request);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PatchMapping("/password/forgot")
    @ResponseBody
    public ResponseEntity<String> resetPassword(
            @Valid @ModelAttribute ForgotPasswordRequest request,
            BindingResult result
    ) {
        if (result.hasErrors()) return ResponseEntity.badRequest().body("Validation error");
        accountService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<String> deleteAccount(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @Valid @ModelAttribute DeleteAccountRequest request,
            BindingResult result
    ) {
        if (result.hasErrors()) return ResponseEntity.badRequest().body("Validation error");
        accountService.deleteAccount(customerDetails.getId(), request);
        return ResponseEntity.ok("Account deactivated successfully");
    }
}