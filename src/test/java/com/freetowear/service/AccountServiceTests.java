package com.freetowear.service;

import com.freetowear.dto.request.account.*;
import com.freetowear.dto.response.account.AddressResponse;
import com.freetowear.dto.response.account.CustomerResponse;
import com.freetowear.entity.Address;
import com.freetowear.entity.Customer;
import com.freetowear.enums.UF;
import com.freetowear.enums.VerificationType;
import com.freetowear.exception.ResourceNotFoundException;
import com.freetowear.repository.AddressRepository;
import com.freetowear.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    
    private static final LocalDate TODAY = LocalDate.now();

    private static CustomerBuilder aCustomer() {
        return new CustomerBuilder();
    }

    private static final class CustomerBuilder {
        private final Customer customer = new Customer();

        CustomerBuilder withEmail(String email) {
            customer.setEmail(email);
            return this;
        }

        CustomerBuilder withPassword(String password) {
            customer.setPassword(password);
            return this;
        }

        CustomerBuilder withEmailVerified(boolean emailVerified) {
            customer.setEmailVerified(emailVerified);
            return this;
        }

        CustomerBuilder withActive(boolean active) {
            customer.setActive(active);
            return this;
        }

        CustomerBuilder withId(String id) {
            customer.setId(id);
            return this;
        }

        CustomerBuilder withName(String name) {
            customer.setName(name);
            return this;
        }

        CustomerBuilder withCpf(String cpf) {
            customer.setCpf(cpf);
            return this;
        }

        CustomerBuilder withPhone(String phone) {
            customer.setPhone(phone);
            return this;
        }

        CustomerBuilder withBirthDate(LocalDate birthDate) {
            customer.setBirthDate(birthDate);
            return this;
        }

        Customer build() {
            return customer;
        }
    }

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificationCodeService verificationCodeService;

    @InjectMocks
    private AccountService accountService;

    @Nested
    @DisplayName("getAccount")
    class GetAccountTests {

        @Test
        @DisplayName("Should return customer response when account is found")
        void shouldReturnCustomerResponseWhenFound() {
            String customerId = "cust-123";
            Customer customer = aCustomer()
                    .withId(customerId)
                    .withName("John Doe")
                    .withEmail("john@example.com")
                    .withPhone("11999999999")
                    .withCpf("12345678901")
                    .withBirthDate(LocalDate.of(1990, 1, 2))
                    .withActive(true)
                    .build();

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

            CustomerResponse response = accountService.getAccount(customerId);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(customerId);
            assertThat(response.getName()).isEqualTo("John Doe");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
            assertThat(response.getPhone()).isEqualTo(customer.getPhone());
            assertThat(response.getCpf()).isEqualTo(customer.getCpf());
            assertThat(response.getBirthDate()).isEqualTo(customer.getBirthDate());
            assertThat(response.getCreatedAt()).isEqualTo(customer.getCreatedAt());
            assertThat(response.getActive()).isEqualTo(customer.getActive());
            verify(customerRepository).findById(customerId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when account is not found")
        void shouldThrowExceptionWhenNotFound() {
            String customerId = "unknown";
            when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getAccount(customerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Account not found with id: " + customerId);
            verify(customerRepository).findById(customerId);
        }
    }

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        @DisplayName("Should save customer with encoded password and terms accepted date")
        void shouldRegisterSuccessfullyWithTermsAccepted() {
            RegisterRequest request = new RegisterRequest();
            request.setName("Jane Doe");
            request.setEmail("jane@example.com");
            request.setPassword("Secret@123");
            request.setTermsAccepted(true);

            when(passwordEncoder.encode("Secret@123")).thenReturn("encodedPassword");

            accountService.register(request);

            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepository).save(captor.capture());

            Customer savedCustomer = captor.getValue();
            assertThat(savedCustomer.getName()).isEqualTo("Jane Doe");
            assertThat(savedCustomer.getEmail()).isEqualTo("jane@example.com");
            assertThat(savedCustomer.getPassword()).isEqualTo("encodedPassword");
            assertThat(savedCustomer.getActive()).isTrue();
            assertThat(savedCustomer.getTermsAndConditionsAcceptedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should reject registration when terms are not accepted")
        void shouldRegisterWithoutTermsDateWhenTermsNotAccepted() {
            RegisterRequest request = new RegisterRequest();
            request.setName("Jane Doe");
            request.setEmail("jane@example.com");
            request.setPassword("Secret@123");
            request.setTermsAccepted(false);

            assertThatThrownBy(() -> accountService.register(request))
                    .isInstanceOf(IllegalArgumentException.class);
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject registration when terms are null")
        void shouldRegisterWithoutTermsDateWhenTermsAreNull() {
            RegisterRequest request = new RegisterRequest();
            request.setName("Jane Doe");
            request.setEmail("jane@example.com");
            request.setPassword("Secret@123");
            request.setTermsAccepted(null);

            assertThatThrownBy(() -> accountService.register(request))
                    .isInstanceOf(IllegalArgumentException.class);
            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateAccount")
    class UpdateAccountTests {

        @Test
        @DisplayName("Should update account details when customer exists and valid age is provided")
        void shouldUpdateAccountSuccessfully() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withId(customerId).withName("Old Name")
                    .withCpf("11111111111").withPhone("11999999999")
                    .withBirthDate(TODAY.minusYears(25)).build();

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

            UpdateAccountRequest request = new UpdateAccountRequest();
            request.setName("New Name");
            request.setCpf("22222222222");
            request.setPhone("11888888888");
            request.setBirthDate(TODAY.minusYears(30).toString());

            accountService.updateAccount(customerId, request);

            assertThat(customer.getName()).isEqualTo("New Name");
            assertThat(customer.getCpf()).isEqualTo("22222222222");
            assertThat(customer.getPhone()).isEqualTo("11888888888");
            assertThat(customer.getBirthDate()).isEqualTo(TODAY.minusYears(30));
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent account")
        void shouldThrowExceptionWhenAccountNotFound() {
            String customerId = "non-existent";
            when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

            UpdateAccountRequest request = new UpdateAccountRequest();
            request.setName("New Name");

            assertThatThrownBy(() -> accountService.updateAccount(customerId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Account not found with id: " + customerId);
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when birth date is in the future")
        void shouldThrowExceptionWhenBirthDateIsInFuture() {
            String customerId = "cust-123";
            Customer customer = aCustomer().build();
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

            UpdateAccountRequest request = new UpdateAccountRequest();
            request.setBirthDate(TODAY.plusDays(1).toString());

            assertThatThrownBy(() -> accountService.updateAccount(customerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Birth date cannot be in the future");
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when customer is under 18 years old")
        void shouldThrowExceptionWhenUnder18() {
            String customerId = "cust-123";
            Customer customer = aCustomer().build();
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

            UpdateAccountRequest request = new UpdateAccountRequest();
            request.setBirthDate(TODAY.minusYears(17).toString());

            assertThatThrownBy(() -> accountService.updateAccount(customerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Must be at least 18 years old");
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when customer is over 150 years old")
        void shouldThrowExceptionWhenOver150() {
            String customerId = "cust-123";
            Customer customer = aCustomer().build();
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

            UpdateAccountRequest request = new UpdateAccountRequest();
            request.setBirthDate(TODAY.minusYears(151).toString());

            assertThatThrownBy(() -> accountService.updateAccount(customerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid birth date");
            verify(customerRepository, never()).save(any());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("birthDateBoundaries")
        @DisplayName("Should apply current age validation at each birth-date boundary")
        void shouldValidateBirthDateBoundaries(String caseName, LocalDate birthDate,
                                                boolean valid, String expectedMessage) {
            String customerId = "cust-123";
            Customer customer = aCustomer().withId(customerId).build();
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            UpdateAccountRequest request = new UpdateAccountRequest();
            request.setBirthDate(birthDate.toString());

            if (valid) {
                accountService.updateAccount(customerId, request);
                assertThat(customer.getBirthDate()).isEqualTo(birthDate);
                verify(customerRepository).save(customer);
            } else {
                assertThatThrownBy(() -> accountService.updateAccount(customerId, request))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage(expectedMessage);
                verify(customerRepository, never()).save(any());
            }
        }

        private static Stream<Arguments> birthDateBoundaries() {
            return Stream.of(
                    Arguments.of("exactly 18 years", TODAY.minusYears(18), true, null),
                    Arguments.of("18 years minus 1 day", TODAY.minusYears(18).plusDays(1), false,
                            "Must be at least 18 years old"),
                    Arguments.of("exactly 150 years", TODAY.minusYears(150), true, null),
                    Arguments.of("150 years plus 1 day (currently accepted)", TODAY.minusYears(150).minusDays(1), true, null),
                    Arguments.of("today", TODAY, false, "Must be at least 18 years old"),
                    Arguments.of("tomorrow", TODAY.plusDays(1), false, "Birth date cannot be in the future")
            );
        }

        @Test
        @DisplayName("Should throw DateTimeParseException when birth date is malformed")
        void shouldThrowExceptionWhenBirthDateIsMalformed() {
            String customerId = "cust-123";
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(aCustomer().build()));

            UpdateAccountRequest request = new UpdateAccountRequest();
            request.setBirthDate("not-a-date");

            assertThatThrownBy(() -> accountService.updateAccount(customerId, request))
                    .isInstanceOf(DateTimeParseException.class);
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not overwrite existing fields if request fields are empty or null")
        void shouldNotOverwriteWhenFieldsAreEmpty() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withName("Keep Name")
                    .withCpf("12345678901").withPhone("11999999999").build();

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

            UpdateAccountRequest request = new UpdateAccountRequest();
            request.setName("");
            request.setCpf("");
            request.setPhone("");
            request.setBirthDate(null);

            accountService.updateAccount(customerId, request);

            assertThat(customer.getName()).isEqualTo("Keep Name");
            assertThat(customer.getCpf()).isEqualTo("12345678901");
            assertThat(customer.getPhone()).isEqualTo("11999999999");
            verify(customerRepository).save(customer);
        }
    }

    @Nested
    @DisplayName("addAddress")
    class AddAddressTests {

        @Test
        @DisplayName("Should save address linked to customer")
        void shouldAddAddressSuccessfully() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withId(customerId).build();

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

            AddAddressRequest request = new AddAddressRequest();
            request.setCep("01001000");
            request.setStreet("Praça da Sé");
            request.setNumber("100");
            request.setComplement("Apt 101");
            request.setNeighborhood("Sé");
            request.setCity("São Paulo");
            request.setState(UF.SP);
            request.setDefaultAddress(true);

            accountService.addAddress(customerId, request);

            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(captor.capture());

            Address savedAddress = captor.getValue();
            assertThat(savedAddress.getCustomer()).isEqualTo(customer);
            assertThat(savedAddress.getCep()).isEqualTo("01001000");
            assertThat(savedAddress.getStreet()).isEqualTo("Praça da Sé");
            assertThat(savedAddress.getNumber()).isEqualTo("100");
            assertThat(savedAddress.getComplement()).isEqualTo("Apt 101");
            assertThat(savedAddress.getNeighborhood()).isEqualTo("Sé");
            assertThat(savedAddress.getCity()).isEqualTo("São Paulo");
            assertThat(savedAddress.getState()).isEqualTo(UF.SP);
            assertThat(savedAddress.getDefaultAddress()).isTrue();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when adding address to non-existent customer")
        void shouldThrowExceptionWhenCustomerNotFound() {
            String customerId = "unknown";
            when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

            AddAddressRequest request = new AddAddressRequest();

            assertThatThrownBy(() -> accountService.addAddress(customerId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Account not found with id: " + customerId);
            verify(addressRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAddresses")
    class GetAddressesTests {

        @Test
        @DisplayName("Should return list of AddressResponse for customer")
        void shouldReturnAddressList() {
            String customerId = "cust-123";
            Address address = new Address();
            address.setId("addr-1");
            address.setStreet("Main Street");
            address.setNumber("123");
            address.setComplement("Suite 4");
            address.setNeighborhood("Downtown");
            address.setCity("City");
            address.setState(UF.SP);
            address.setCep("12345678");
            address.setDefaultAddress(true);

            when(addressRepository.findAllByCustomerId(customerId)).thenReturn(List.of(address));
            when(customerRepository.existsById(customerId)).thenReturn(true);

            List<AddressResponse> responses = accountService.getAddresses(customerId);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getId()).isEqualTo("addr-1");
            assertThat(responses.get(0).getCep()).isEqualTo("12345678");
            assertThat(responses.get(0).getStreet()).isEqualTo("Main Street");
            assertThat(responses.get(0).getNumber()).isEqualTo("123");
            assertThat(responses.get(0).getComplement()).isEqualTo("Suite 4");
            assertThat(responses.get(0).getNeighborhood()).isEqualTo("Downtown");
            assertThat(responses.get(0).getCity()).isEqualTo("City");
            assertThat(responses.get(0).getState()).isEqualTo(UF.SP);
            assertThat(responses.get(0).getDefaultAddress()).isTrue();
        }

        @Test
        @DisplayName("Should return empty list when no addresses exist")
        void shouldReturnEmptyListWhenNoAddresses() {
            String customerId = "cust-123";
            when(addressRepository.findAllByCustomerId(customerId)).thenReturn(Collections.emptyList());
            when(customerRepository.existsById(customerId)).thenReturn(true);

            List<AddressResponse> responses = accountService.getAddresses(customerId);

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("changeEmail")
    class ChangeEmailTests {

        @Test
        @DisplayName("Should change email successfully when password and current email match and new email is available")
        void shouldChangeEmailSuccessfully() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withEmail("old@example.com")
                    .withPassword("encodedPassword").build();

            ChangeEmailRequest request = new ChangeEmailRequest();
            request.setCurrentEmail("old@example.com");
            request.setNewEmail("new@example.com");
            request.setPassword("plainPassword");

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);
            when(customerRepository.existsByEmail("new@example.com")).thenReturn(false);

            accountService.changeEmail(customerId, request);

            assertThat(customer.getEmail()).isEqualTo("new@example.com");
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer is not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            String customerId = "unknown";
            ChangeEmailRequest request = new ChangeEmailRequest();

            when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.changeEmail(customerId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when current email does not match")
        void shouldThrowExceptionWhenCurrentEmailDoesNotMatch() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withEmail("actual@example.com").build();

            ChangeEmailRequest request = new ChangeEmailRequest();
            request.setCurrentEmail("different@example.com");

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

            assertThatThrownBy(() -> accountService.changeEmail(customerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Current email does not match");
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when password does not match")
        void shouldThrowExceptionWhenPasswordDoesNotMatch() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withEmail("old@example.com")
                    .withPassword("encodedPassword").build();

            ChangeEmailRequest request = new ChangeEmailRequest();
            request.setCurrentEmail("old@example.com");
            request.setNewEmail("new@example.com");
            request.setPassword("wrongPassword");

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            assertThatThrownBy(() -> accountService.changeEmail(customerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid password");
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when new email is already in use")
        void shouldThrowExceptionWhenNewEmailAlreadyInUse() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withEmail("old@example.com")
                    .withPassword("encodedPassword").build();

            ChangeEmailRequest request = new ChangeEmailRequest();
            request.setCurrentEmail("old@example.com");
            request.setNewEmail("existing@example.com");
            request.setPassword("correctPassword");

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(passwordEncoder.matches("correctPassword", "encodedPassword")).thenReturn(true);
            when(customerRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThatThrownBy(() -> accountService.changeEmail(customerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already in use");
            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully when current password matches")
        void shouldChangePasswordSuccessfully() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withPassword("oldEncoded").build();

            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("oldPlain");
            request.setNewPassword("NewPlain@123");

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(passwordEncoder.matches("oldPlain", "oldEncoded")).thenReturn(true);
            when(passwordEncoder.encode("NewPlain@123")).thenReturn("newEncoded");

            accountService.changePassword(customerId, request);

            assertThat(customer.getPassword()).isEqualTo("newEncoded");
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer is not found")
        void shouldThrowExceptionWhenNotFound() {
            String customerId = "unknown";
            ChangePasswordRequest request = new ChangePasswordRequest();

            when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.changePassword(customerId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when current password does not match")
        void shouldThrowExceptionWhenCurrentPasswordIncorrect() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withPassword("oldEncoded").build();

            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("wrongPassword");

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(passwordEncoder.matches("wrongPassword", "oldEncoded")).thenReturn(false);

            assertThatThrownBy(() -> accountService.changePassword(customerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Current password is incorrect");
            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password and verify code when customer exists")
        void shouldResetPasswordSuccessfully() {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setContact("user@example.com");
            request.setCode("123456");
            request.setNewPassword("BrandNewPassword@123");

            Customer customer = aCustomer().withEmail("user@example.com").build();

            when(customerRepository.findByEmail("user@example.com")).thenReturn(Optional.of(customer));
            when(passwordEncoder.encode("BrandNewPassword@123")).thenReturn("encodedNewPassword");

            accountService.resetPassword(request);

            verify(verificationCodeService).verify(customer, VerificationType.PASSWORD_RESET, "123456");
            assertThat(customer.getPassword()).isEqualTo("encodedNewPassword");
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("Should not save password when verification code is rejected")
        void shouldNotSaveWhenVerificationCodeIsRejected() {
            ForgotPasswordRequest request = new ForgotPasswordRequest("user@example.com", "bad-code", "BrandNewPassword@123");
            Customer customer = aCustomer().build();
            when(customerRepository.findByEmail("user@example.com")).thenReturn(Optional.of(customer));
            doThrow(new IllegalArgumentException("Código inválido"))
                    .when(verificationCodeService)
                    .verify(customer, VerificationType.PASSWORD_RESET, "bad-code");

            assertThatThrownBy(() -> accountService.resetPassword(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Código inválido");
            verify(passwordEncoder, never()).encode(any());
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when contact not found")
        void shouldThrowExceptionWhenContactNotFound() {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setContact("unknown@example.com");

            when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.resetPassword(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Contato ou código inválido");
            verifyNoInteractions(verificationCodeService);
            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("requestPasswordReset")
    class RequestPasswordResetTests {

        @Test
        @DisplayName("Should send reset code when customer exists")
        void shouldSendCodeWhenCustomerExists() {
            String email = "user@example.com";
            Customer customer = aCustomer().withEmail(email).build();

            when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));

            accountService.requestPasswordReset(email);

            verify(verificationCodeService).sendPasswordResetCode(customer);
        }

        @Test
        @DisplayName("Should do nothing when customer does not exist")
        void shouldDoNothingWhenCustomerNotFound() {
            String email = "unknown@example.com";
            when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

            accountService.requestPasswordReset(email);

            verifyNoInteractions(verificationCodeService);
        }
    }

    @Nested
    @DisplayName("verifyPasswordResetCode")
    class VerifyPasswordResetCodeTests {

        @Test
        @DisplayName("Should check code when customer exists")
        void shouldCheckCodeSuccessfully() {
            String email = "user@example.com";
            String code = "123456";
            Customer customer = aCustomer().build();

            when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));

            accountService.verifyPasswordResetCode(email, code);

            verify(verificationCodeService).checkCode(customer, VerificationType.PASSWORD_RESET, code);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when customer does not exist")
        void shouldThrowExceptionWhenCustomerNotFound() {
            String email = "unknown@example.com";
            when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.verifyPasswordResetCode(email, "123456"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("E-mail ou código inválido");
            verifyNoInteractions(verificationCodeService);
        }
    }

    @Nested
    @DisplayName("completePasswordReset")
    class CompletePasswordResetTests {

        @Test
        @DisplayName("Should complete password reset successfully")
        void shouldCompletePasswordResetSuccessfully() {
            String email = "user@example.com";
            String code = "123456";
            String newPassword = "NewPassword@123";
            Customer customer = aCustomer().build();

            when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
            when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

            accountService.completePasswordReset(email, code, newPassword);

            verify(verificationCodeService).verify(customer, VerificationType.PASSWORD_RESET, code);
            assertThat(customer.getPassword()).isEqualTo("encodedNewPassword");
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.completePasswordReset("unknown@example.com", "123456", "pwd"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("E-mail ou código inválido");
            verifyNoInteractions(verificationCodeService);
            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteAccount")
    class DeleteAccountTests {

        @Test
        @DisplayName("Should deactivate customer account when password matches")
        void shouldDeactivateAccountSuccessfully() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withPassword("encodedPassword")
                    .withActive(true).build();

            DeleteAccountRequest request = new DeleteAccountRequest();
            request.setPassword("rawPassword");

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

            accountService.deleteAccount(customerId, request);

            assertThat(customer.getActive()).isFalse();
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            String customerId = "unknown";
            DeleteAccountRequest request = new DeleteAccountRequest();

            when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.deleteAccount(customerId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when password does not match")
        void shouldThrowExceptionWhenPasswordDoesNotMatch() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withPassword("encodedPassword").build();

            DeleteAccountRequest request = new DeleteAccountRequest();
            request.setPassword("wrongPassword");

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            assertThatThrownBy(() -> accountService.deleteAccount(customerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid password");
            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("requestEmailVerification")
    class RequestEmailVerificationTests {

        @Test
        @DisplayName("Should return verification code when email is not yet verified")
        void shouldReturnCodeWhenEmailNotVerified() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withEmailVerified(false).build();

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(verificationCodeService.generate(customer, VerificationType.EMAIL)).thenReturn("654321");

            String code = accountService.requestEmailVerification(customerId);

            assertThat(code).isEqualTo("654321");
            verify(verificationCodeService).generate(customer, VerificationType.EMAIL);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer is not found")
        void shouldThrowExceptionWhenNotFound() {
            String customerId = "unknown";
            when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.requestEmailVerification(customerId))
                    .isInstanceOf(ResourceNotFoundException.class);
            verifyNoInteractions(verificationCodeService);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when email is already verified")
        void shouldThrowExceptionWhenAlreadyVerified() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withEmailVerified(true).build();

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

            assertThatThrownBy(() -> accountService.requestEmailVerification(customerId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already verified");
            verifyNoInteractions(verificationCodeService);
        }
    }

    @Nested
    @DisplayName("verifyEmail")
    class VerifyEmailTests {

        @Test
        @DisplayName("Should verify code, mark email as verified, and save customer")
        void shouldVerifyEmailSuccessfully() {
            String customerId = "cust-123";
            String code = "123456";
            Customer customer = aCustomer().withEmailVerified(false).build();

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

            accountService.verifyEmail(customerId, code);

            verify(verificationCodeService).verify(customer, VerificationType.EMAIL, code);
            assertThat(customer.isEmailVerified()).isTrue();
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("Should not mark or save customer when email code is rejected")
        void shouldNotVerifyEmailWhenCodeIsRejected() {
            String customerId = "cust-123";
            Customer customer = aCustomer().build();
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            doThrow(new IllegalArgumentException("Código inválido"))
                    .when(verificationCodeService)
                    .verify(customer, VerificationType.EMAIL, "bad-code");

            assertThatThrownBy(() -> accountService.verifyEmail(customerId, "bad-code"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Código inválido");
            assertThat(customer.isEmailVerified()).isFalse();
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer is not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            String customerId = "unknown";
            when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.verifyEmail(customerId, "123456"))
                    .isInstanceOf(ResourceNotFoundException.class);
            verifyNoInteractions(verificationCodeService);
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when email is already verified")
        void shouldThrowExceptionWhenAlreadyVerified() {
            String customerId = "cust-123";
            Customer customer = aCustomer().withEmailVerified(true).build();

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

            assertThatThrownBy(() -> accountService.verifyEmail(customerId, "123456"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already verified");
            verifyNoInteractions(verificationCodeService);
            verify(customerRepository, never()).save(any());
        }
    }
}