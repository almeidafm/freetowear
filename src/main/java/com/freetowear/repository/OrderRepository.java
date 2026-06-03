package com.freetowear.repository;

import com.freetowear.entity.Customer;
import com.freetowear.entity.Order;
import com.freetowear.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findAllByCustomerId(String customerId);
    Optional<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);
    List<Order> findByCustomer(Customer customer);
}