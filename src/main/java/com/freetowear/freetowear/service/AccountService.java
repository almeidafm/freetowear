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
        customer.setPasswordHash(request.getPassword()); // hash password here later with Spring Security
        customer.setCpf(request.getCpf());
        customer.setPhone(request.getPhone());
        if (request.getBirthDate() != null && !request.getBirthDate().isEmpty())
            customer.setBirthDate(LocalDate.parse(request.getBirthDate()));
        customer.setActive(true); // client cannot control this
        customerRepository.save(customer);
    }

    public void deleteAccount(Integer id) {
        customerRepository.findById(id).ifPresent(customer -> {
            customer.setActive(false); // soft delete, keeps data in the database
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
                customer.setBirthDate(LocalDate.parse(request.getBirthDate()));
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
}