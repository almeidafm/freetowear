package com.freetowear.freetowear.repository;

import com.freetowear.freetowear.entity.Customer;
import com.freetowear.freetowear.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByCustomer(Customer customer);

}
