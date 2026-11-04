package com.freetowear.service;

import com.freetowear.entity.Customer;
import com.freetowear.entity.VerificationCode;
import com.freetowear.enums.VerificationType;
import com.freetowear.infra.EmailService;
import com.freetowear.repository.CustomerRepository;
import com.freetowear.repository.VerificationCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceTest {

    @Mock private VerificationCodeRepository verificationCodeRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private EmailService emailService;
    @InjectMocks private VerificationCodeService verificationCodeService;

    private Customer customer() {
        Customer customer = new Customer();
        customer.setId("customer-1");
        customer.setEmail("jane@example.com");
        return customer;
    }

    private VerificationCode code(String value, int attempts) {
        VerificationCode code = new VerificationCode();
        code.setCustomer(customer());
        code.setType(VerificationType.EMAIL);
        code.setCode(value);
        code.setAttempts(attempts);
        code.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        return code;
    }

    @Test
    void shouldGenerateSixDigitCodeAndInvalidatePreviousCodes() {
        Customer customer = customer();
        VerificationCode previous = code("111111", 0);
        when(verificationCodeRepository.findByCustomerAndTypeAndUsedFalseAndInvalidatedFalse(
                customer, VerificationType.EMAIL)).thenReturn(List.of(previous));

        String result = verificationCodeService.generate(customer, VerificationType.EMAIL);

        assertThat(result).matches("\\d{6}");
        assertThat(previous.isInvalidated()).isTrue();
        verify(verificationCodeRepository).saveAll(List.of(previous));
        ArgumentCaptor<VerificationCode> captor = ArgumentCaptor.forClass(VerificationCode.class);
        verify(verificationCodeRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo(result);
        assertThat(captor.getValue().getCustomer()).isSameAs(customer);
        assertThat(captor.getValue().getType()).isEqualTo(VerificationType.EMAIL);
        assertThat(captor.getValue().getAttempts()).isZero();
        assertThat(captor.getValue().getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void shouldSendEmailVerificationCode() {
        Customer customer = customer();
        when(verificationCodeRepository.findByCustomerAndTypeAndUsedFalseAndInvalidatedFalse(
                customer, VerificationType.EMAIL)).thenReturn(List.of());

        verificationCodeService.sendEmailVerificationCode(customer);

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(org.mockito.ArgumentMatchers.eq(customer.getEmail()),
                codeCaptor.capture());
        assertThat(codeCaptor.getValue()).matches("\\d{6}");
    }

    @Test
    void shouldIncreaseAttemptsAndRejectWrongCode() {
        Customer customer = customer();
        VerificationCode verificationCode = code("123456", 1);
        when(verificationCodeRepository
                .findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        any(), any(), any())).thenReturn(Optional.of(verificationCode));

        assertThatThrownBy(() -> verificationCodeService.verify(customer, VerificationType.EMAIL, "000000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código inválido");

        assertThat(verificationCode.getAttempts()).isEqualTo(2);
        verify(verificationCodeRepository).save(verificationCode);
        assertThat(verificationCode.isUsed()).isFalse();
    }

    @Test
    void shouldMarkCodeUsedWhenVerificationSucceeds() {
        Customer customer = customer();
        VerificationCode verificationCode = code("123456", 0);
        when(verificationCodeRepository
                .findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        any(), any(), any())).thenReturn(Optional.of(verificationCode));

        verificationCodeService.verify(customer, VerificationType.EMAIL, "123456");

        assertThat(verificationCode.getAttempts()).isEqualTo(1);
        assertThat(verificationCode.isUsed()).isTrue();
        verify(verificationCodeRepository).save(verificationCode);
    }

    @Test
    void shouldVerifyEmailAndMarkCustomerAsVerified() {
        Customer customer = customer();
        VerificationCode verificationCode = code("123456", 0);
        when(verificationCodeRepository
                .findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        any(), any(), any())).thenReturn(Optional.of(verificationCode));

        verificationCodeService.verifyEmail(customer, "123456");

        assertThat(customer.isEmailVerified()).isTrue();
        verify(customerRepository).save(customer);
    }

    @Test
    void shouldRejectExpiredOrMissingCode() {
        Customer customer = customer();
        when(verificationCodeRepository
                .findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        any(), any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificationCodeService.checkCode(customer, VerificationType.EMAIL, "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código inválido ou expirado");
        verify(verificationCodeRepository, never()).save(any());
        verifyNoInteractions(customerRepository);
    }

    @Test
    void shouldRejectCodeAfterMaximumAttempts() {
        Customer customer = customer();
        VerificationCode verificationCode = code("123456", 5);
        when(verificationCodeRepository
                .findFirstByCustomerAndTypeAndUsedFalseAndInvalidatedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        any(), any(), any())).thenReturn(Optional.of(verificationCode));

        assertThatThrownBy(() -> verificationCodeService.checkCode(customer, VerificationType.EMAIL, "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número máximo de tentativas excedido");
        verify(verificationCodeRepository, never()).save(any());
    }
}