package com.freetowear.repository;

import com.freetowear.entity.OrderItem;
import com.freetowear.entity.ProductVariation;
import com.freetowear.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findAllByOrderId(String orderId);

    boolean existsByOrderCustomerIdAndProductIdAndOrderStatusIn(
            String customerId,
            String productId,
            Collection<OrderStatus> statuses
    );

    @Query("SELECT DISTINCT oi.productVariation FROM OrderItem oi " +
           "WHERE oi.order.customer.id = :customerId " +
           "AND oi.product.id = :productId " +
           "AND oi.order.status IN :statuses")
    List<ProductVariation> findPurchasedVariationsByCustomerAndProduct(
            @Param("customerId") String customerId,
            @Param("productId") String productId,
            @Param("statuses") Collection<OrderStatus> statuses
    );
}