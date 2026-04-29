package com.freetowear.freetowear.repository;

import com.freetowear.freetowear.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

}
