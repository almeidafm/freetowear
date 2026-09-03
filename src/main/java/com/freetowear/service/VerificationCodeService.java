package com.freetowear.service;

import com.freetowear.entity.Customer;
import com.freetowear.entity.VerificationCode;
import com.freetowear.enums.VerificationType;
import com.freetowear.infra.EmailService;
import com.freetowear.repository.CustomerRepository;
import com.freetowear.repository.VerificationCodeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final VerificationCodeRepository verificationCodeRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String generate(
            Customer customer,
            VerificationType type
    ) {
        invalidatePreviousCodes(customer, type);

        String code = generateCode();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCustomer(customer);
        verificationCode.setType(type);
        verificationCode.setCode(code);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)
        );

        verificationCodeRepository.save(verificationCode);

        return code;
    }

    @Transactional
    public void sendEmailVerificationCode(Customer customer) {
        String code = generate(customer, VerificationType.EMAIL);
        emailService.sendVerificationEmail(customer.getEmail(), code);
    }

    @Transactional
    public void verify(
            Customer customer,
            VerificationType type,
            String code
    ) {
        VerificationCode verificationCode =
                verificationCodeRepository
                        .findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                                customer,
                                type,
                                LocalDateTime.now()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Código inválido ou expirado"
                                )
                        );

        if (verificationCode.getAttempts() >= MAX_ATTEMPTS) {
            throw new IllegalArgumentException(
                    "Número máximo de tentativas excedido"
            );
        }

        verificationCode.setAttempts(
                verificationCode.getAttempts() + 1
        );

        if (!verificationCode.getCode().equals(code)) {
            verificationCodeRepository.save(verificationCode);

            throw new IllegalArgumentException(
                    "Código inválido"
            );
        }

        verificationCode.setUsed(true);

        verificationCodeRepository.save(verificationCode);
    }

    @Transactional
    public void verifyEmail(Customer customer, String code) {
        verify(customer, VerificationType.EMAIL, code);
        customer.setEmailVerified(true);
        customerRepository.save(customer);
    }

    private void invalidatePreviousCodes(
            Customer customer,
            VerificationType type
    ) {
        List<VerificationCode> codes =
                verificationCodeRepository.findByCustomerAndTypeAndUsedFalseAndInvalidatedFalse(
                        customer,
                        type
                );

        codes.forEach(code -> code.setInvalidated(true));

        verificationCodeRepository.saveAll(codes);
    }

    private String generateCode() {
        int number = secureRandom.nextInt(1_000_000);
        return String.format("%06d", number);
    }
}