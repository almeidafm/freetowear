package com.freetowear.freetowear.repository;

import com.freetowear.freetowear.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    boolean existsByEmail(String newEmail);

    Optional<Customer> findByEmailOrPhone(String email, String phone);
    Optional<Customer> findByEmailAndPasswordHash(String email, String passwordHash);
    Optional<Customer> findByEmail(String email);
}


