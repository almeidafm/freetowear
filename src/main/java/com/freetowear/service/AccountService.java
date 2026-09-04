package com.freetowear.service;

import com.freetowear.dto.response.account.AddressResponse;
import com.freetowear.entity.Address;
import com.freetowear.entity.Customer;
import com.freetowear.entity.VerificationCode;
import com.freetowear.enums.VerificationType;
import com.freetowear.repository.AddressRepository;
import com.freetowear.repository.CustomerRepository;
import com.freetowear.dto.request.account.*;
import com.freetowear.dto.request.account.*;
import com.freetowear.dto.response.account.CustomerResponse;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private  VerificationCodeService verificationCodeService;

    public CustomerResponse getAccount(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        return new CustomerResponse(customer);
    }

    public void register(RegisterRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setActive(true);
        customerRepository.save(customer);
    }

    public void updateAccount(String id, UpdateAccountRequest request) {
        customerRepository.findById(id).ifPresent(customer -> {
            if (request.getName() != null && !request.getName().isEmpty())
                customer.setName(request.getName());
            if (request.getCpf() != null && !request.getCpf().isEmpty())
                customer.setCpf(request.getCpf());
            if (request.getBirthDate() != null && !request.getBirthDate().isEmpty())
                customer.setBirthDate(parseAndValidateAge(request.getBirthDate()));
            if (request.getPhone() != null && !request.getPhone().isEmpty())
                customer.setPhone(request.getPhone());
            customerRepository.save(customer);
        });
    }

    public void addAddress(String id, AddAddressRequest request) {
        customerRepository.findById(id).ifPresent(customer -> {
            Address address = new Address();
            address.setCustomer(customer);
            address.setCep(request.getCep());
            address.setStreet(request.getStreet());
            address.setNumber(request.getNumber());
            address.setComplement(request.getComplement());
            address.setNeighborhood(request.getNeighborhood());
            address.setCity(request.getCity());
            address.setState(request.getState());
            address.setDefaultAddress(request.getDefaultAddress());
            addressRepository.save(address);
        });
    }

    public List<AddressResponse> getAddresses(String idCustomer) {
        return addressRepository.findAllByCustomerId(idCustomer)
                .stream()
                .map(AddressResponse::new)
                .toList();
    }

    private LocalDate parseAndValidateAge(String birthDate) {
        LocalDate birth = LocalDate.parse(birthDate);

        boolean isBirthDateInFuture = birth.isAfter(LocalDate.now());
        if (isBirthDateInFuture) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }

        long age = ChronoUnit.YEARS.between(birth, LocalDate.now());

        boolean isUnder18 = age < 18;
        if (isUnder18) {
            throw new IllegalArgumentException("Must be at least 18 years old");
        }

        boolean isOver150 = age > 150;
        if (isOver150) {
            throw new IllegalArgumentException("Invalid birth date");
        }

        return birth;
    }

    public void changeEmail(String id, ChangeEmailRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!customer.getEmail().equals(request.getCurrentEmail())) {
            throw new IllegalArgumentException("Current email does not match");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                customer.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        if (customerRepository.existsByEmail(request.getNewEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        customer.setEmail(request.getNewEmail());
        customerRepository.save(customer);
    }

    public void changePassword(String id, ChangePasswordRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), customer.getPassword()))
            throw new IllegalArgumentException("Current password is incorrect");

        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);
    }

    @Transactional
    public void resetPassword(ForgotPasswordRequest request) {

        Customer customer = customerRepository.findByEmail(request.getContact())
                .orElseThrow(() -> new IllegalArgumentException("Contato ou código inválido"));

        verificationCodeService.verify(customer, VerificationType.PASSWORD_RESET, request.getCode());

        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);
    }

    public void requestPasswordReset(String email) {
        customerRepository.findByEmail(email).ifPresent(customer -> {
            verificationCodeService.sendPasswordResetCode(customer);
        });
    }

    public void verifyPasswordResetCode(String email, String code) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou código inválido"));

        verificationCodeService.checkCode(customer, VerificationType.PASSWORD_RESET, code);
    }

    @Transactional
    public void completePasswordReset(String email, String code, String newPassword) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou código inválido"));

        verificationCodeService.verify(customer, VerificationType.PASSWORD_RESET, code);

        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
    }

    public void deleteAccount(String id, DeleteAccountRequest request) {
        customerRepository.findById(id).ifPresent(customer -> {
            if (!passwordEncoder.matches(request.getPassword(), customer.getPassword()))
                throw new IllegalArgumentException("Invalid password");

            customer.setActive(false);
            customerRepository.save(customer);
        });
    }

    public String requestEmailVerification(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found")
                );

        if (customer.isEmailVerified()) {
            throw new IllegalArgumentException("Email already verified");
        }

        return verificationCodeService.generate(
                customer,
                VerificationType.EMAIL
        );
    }

    public void verifyEmail(String id, String code) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found")
                );

        if (customer.isEmailVerified()) {
            throw new IllegalArgumentException("Email already verified");
        }

        verificationCodeService.verify(
                customer,
                VerificationType.EMAIL,
                code
        );

        customer.setEmailVerified(true);
        customerRepository.save(customer);
    }
}