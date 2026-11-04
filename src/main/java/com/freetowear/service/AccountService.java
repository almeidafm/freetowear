package com.freetowear.service;

import com.freetowear.dto.response.account.AddressResponse;
import com.freetowear.entity.Address;
import com.freetowear.entity.Customer;
import com.freetowear.enums.VerificationType;
import com.freetowear.repository.AddressRepository;
import com.freetowear.repository.CustomerRepository;
import com.freetowear.dto.request.account.*;
import com.freetowear.dto.response.account.CustomerResponse;
import com.freetowear.exception.ResourceNotFoundException;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AccountService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$");

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
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        return new CustomerResponse(customer);
    }

    public void register(RegisterRequest request) {
        validateRegistration(request);
        if (customerRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email already in use");
        }
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail().trim());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setTermsAndConditionsAcceptedAt(LocalDateTime.now());
        customer.setActive(true);
        customerRepository.save(customer);
    }

    public void updateAccount(String id, UpdateAccountRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        if (request.getName() != null && !request.getName().isEmpty())
            customer.setName(request.getName());
        if (request.getCpf() != null && !request.getCpf().isEmpty())
            customer.setCpf(request.getCpf());
        if (request.getBirthDate() != null && !request.getBirthDate().isEmpty())
            customer.setBirthDate(parseAndValidateAge(request.getBirthDate()));
        if (request.getPhone() != null && !request.getPhone().isEmpty())
            customer.setPhone(request.getPhone());
        customerRepository.save(customer);
    }

    public void addAddress(String id, AddAddressRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        validateAddress(request);

        Address address = new Address();
        address.setCustomer(customer);
        address.setCep(request.getCep());
        address.setStreet(request.getStreet());
        address.setNumber(request.getNumber());
        address.setComplement(request.getComplement());
        address.setNeighborhood(request.getNeighborhood());
        address.setCity(request.getCity());
        address.setState(request.getState());
        boolean isDefault = Boolean.TRUE.equals(request.getDefaultAddress());
        if (isDefault) {
            List<Address> addresses = addressRepository.findAllByCustomerId(id);
            addresses.forEach(existing -> existing.setDefaultAddress(false));
            addressRepository.saveAll(addresses);
        }
        address.setDefaultAddress(isDefault);
        addressRepository.save(address);
    }

    public List<AddressResponse> getAddresses(String idCustomer) {
        if (!customerRepository.existsById(idCustomer)) {
            throw new ResourceNotFoundException("Account not found with id: " + idCustomer);
        }
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
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        if (request == null || !customer.getEmail().equals(request.getCurrentEmail())) {
            throw new IllegalArgumentException("Current email does not match");
        }

        if (!isValidEmail(request.getCurrentEmail())
                || request.getPassword() == null || request.getPassword().isBlank()
                || !isValidEmail(request.getNewEmail())) {
            throw new IllegalArgumentException("Invalid email change request");
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
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        if (request == null || request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), customer.getPassword()))
            throw new IllegalArgumentException("Current password is incorrect");

        validateNewPassword(request.getNewPassword());

        if (passwordEncoder.matches(request.getNewPassword(), customer.getPassword())) {
            throw new IllegalArgumentException("New password must be different");
        }

        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);
    }

    @Transactional
    public void resetPassword(ForgotPasswordRequest request) {
        if (request == null) throw new IllegalArgumentException("Contato ou código inválido");
        Customer customer = customerRepository.findByEmail(request.getContact())
                .orElseThrow(() -> new IllegalArgumentException("Contato ou código inválido"));

        validateNewPassword(request.getNewPassword());

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

        validateNewPassword(newPassword);

        verificationCodeService.verify(customer, VerificationType.PASSWORD_RESET, code);

        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
    }

    public void deleteAccount(String id, DeleteAccountRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        if (!Boolean.TRUE.equals(customer.getActive())) {
            throw new IllegalArgumentException("Account is already inactive");
        }
        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword()))
            throw new IllegalArgumentException("Invalid password");

        customer.setActive(false);
        customerRepository.save(customer);
    }

    public String requestEmailVerification(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found with id: " + id)
                );

        if (customer.isEmailVerified()) {
            throw new IllegalArgumentException("Email already verified");
        }

        return verificationCodeService.generate(
                customer,
                VerificationType.EMAIL
        );
    }

    @Transactional
    public void verifyEmail(String id, String code) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found with id: " + id)
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

    private void validateRegistration(RegisterRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()
                || !isValidEmail(request.getEmail()) || !isValidPassword(request.getPassword())
                || !Boolean.TRUE.equals(request.getTermsAccepted())) {
            throw new IllegalArgumentException("Invalid registration data");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    private boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    private void validateNewPassword(String password) {
        if (!isValidPassword(password)) throw new IllegalArgumentException("Invalid password");
    }

    private void validatePasswordChange(ChangePasswordRequest request) {
        if (request == null || request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }
        validateNewPassword(request.getNewPassword());
    }

    private void validateAddress(AddAddressRequest request) {
        if (request == null || request.getCep() == null || !request.getCep().matches("\\d{8}")
                || request.getStreet() == null || request.getStreet().isBlank()
                || request.getNeighborhood() == null || request.getNeighborhood().isBlank()
                || request.getCity() == null || request.getCity().isBlank()
                || request.getState() == null) {
            throw new IllegalArgumentException("Invalid address data");
        }
    }
}