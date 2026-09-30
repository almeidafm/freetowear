package com.freetowear.repository;

import com.freetowear.entity.OrderItem;
import com.freetowear.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Collection;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findAllByOrderId(String orderId);

    boolean existsByOrderCustomerIdAndProductIdAndOrderStatusIn(
            String customerId,
            String productId,
            Collection<OrderStatus> statuses
    );
}