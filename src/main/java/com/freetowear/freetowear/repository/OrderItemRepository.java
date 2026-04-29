package com.freetowear.freetowear.repository;

import com.freetowear.freetowear.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

}