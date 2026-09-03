package com.freetowear.controller.api.customer;

import com.freetowear.dto.request.verification.VerificationCodeRequest;
import com.freetowear.infra.security.CustomerDetails;
import com.freetowear.service.VerificationCodeService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@RestController
@RequestMapping("/email-verification")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final VerificationCodeService verificationCodeService;

    @PostMapping("/send")
    public ResponseEntity<?> sendCode(
            @AuthenticationPrincipal CustomerDetails customerDetails
    ) {
        verificationCodeService.sendEmailVerificationCode(
                customerDetails.getCustomer()
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Código enviado para seu e-mail."
                )
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(
            @RequestBody VerificationCodeRequest request,
            @AuthenticationPrincipal CustomerDetails customerDetails
    ) {
        try {
            verificationCodeService.verifyEmail(
                    customerDetails.getCustomer(),
                    request.code()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "E-mail verificado com sucesso."
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        }
    }
}