package com.freetowear.repository;

import com.freetowear.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    boolean existsByEmail(String newEmail);
    Optional<Customer> findByEmailOrPhone(String email, String phone);
    Optional<Customer> findByEmail(String email);
}