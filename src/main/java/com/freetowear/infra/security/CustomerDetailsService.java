package com.freetowear.infra.security;

import com.freetowear.entity.Customer;
import com.freetowear.repository.CustomerRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomerDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public CustomerDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        Customer customer = customerRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found: "));

        return new CustomerDetails(customer);
    }
}
