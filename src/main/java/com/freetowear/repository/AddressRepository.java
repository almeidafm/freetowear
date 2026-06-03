package com.freetowear.repository;

import com.freetowear.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, String> {
    List<Address> findAllByCustomerId(String customerId);
}