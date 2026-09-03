package com.freetowear.repository;

import com.freetowear.entity.Customer;
import com.freetowear.entity.VerificationCode;
import com.freetowear.enums.VerificationType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, String> {

    Optional<VerificationCode>
    findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseOrderByCreatedAtDesc(
            Customer customer,
            VerificationType type
    );

    List<VerificationCode>
    findByCustomerAndTypeAndUsedFalseAndInvalidatedFalse(
            Customer customer,
            VerificationType type
    );

    Optional<VerificationCode>
    findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            Customer customer,
            VerificationType type,
            LocalDateTime now
    );
}