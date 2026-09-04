package com.freetowear.controller.api.customer;

import com.freetowear.dto.request.account.ForgotPasswordRequest;
import com.freetowear.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/password")
public class PasswordWebController {

    private final AccountService accountService;

    public PasswordWebController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/request-code")
    @ResponseBody
    public ResponseEntity<?> requestCode(@RequestBody Map<String, String> payload) {
        String contact = payload.get("contact");
        accountService.requestPasswordReset(contact);
        return ResponseEntity.ok(Map.of("message", "Se o contato existir, um código foi enviado."));
    }

    @PostMapping("/verify-code")
    @ResponseBody
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> payload) {
        String contact = payload.get("contact");
        String code = payload.get("code");
        accountService.verifyPasswordResetCode(contact, code);
        return ResponseEntity.ok(Map.of("message", "Código verificado com sucesso."));
    }

    @PostMapping("/reset")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        accountService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso!"));
    }
}