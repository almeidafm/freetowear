package com.freetowear.freetowear.service;

import com.freetowear.freetowear.dto.request.account.AddAddressRequest;
import com.freetowear.freetowear.dto.request.account.RegisterRequest;
import com.freetowear.freetowear.dto.request.account.UpdateAccountRequest;
import com.freetowear.freetowear.entity.Address;
import com.freetowear.freetowear.entity.Customer;
import com.freetowear.freetowear.repository.AddressRepository;
import com.freetowear.freetowear.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class AccountService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    public void register(RegisterRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPasswordHash(request.getPassword());
        customer.setCpf(request.getCpf());
        customer.setPhone(request.getPhone());
        if (request.getBirthDate() != null && !request.getBirthDate().isEmpty())
            customer.setBirthDate(parseAndValidateAge(request.getBirthDate()));
        customer.setActive(true);
        customerRepository.save(customer);
    }

    public void deleteAccount(Integer id) {
        customerRepository.findById(id).ifPresent(customer -> {
            customer.setActive(false);
            customerRepository.save(customer);
        });
    }

    public void updateAccount(Integer id, UpdateAccountRequest request) {
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

    public void addAddress(Integer id, AddAddressRequest request) {
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

    private LocalDate parseAndValidateAge(String birthDate) {
        LocalDate birth = LocalDate.parse(birthDate);
        if (birth.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Birth date cannot be in the future");
        long age = ChronoUnit.YEARS.between(birth, LocalDate.now());
        if (age < 18)
            throw new IllegalArgumentException("Must be at least 18 years old");
        if (age > 150)
            throw new IllegalArgumentException("Invalid birth date");
        return birth;
    }
}