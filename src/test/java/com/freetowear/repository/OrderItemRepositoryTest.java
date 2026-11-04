package com.freetowear.repository;

import com.freetowear.entity.Category;
import com.freetowear.entity.Customer;
import com.freetowear.entity.Order;
import com.freetowear.entity.OrderItem;
import com.freetowear.entity.Product;
import com.freetowear.entity.ProductVariation;
import com.freetowear.enums.OrderStatus;
import com.freetowear.enums.Size;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemRepositoryTest extends RepositoryTestBase {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnEmptyWhenOrderHasNoItems() {
        Order order = persistOrder("customer@mail.com", OrderStatus.PAID, LocalDateTime.of(2026, 1, 1, 12, 0));
        flushAndClear();

        assertThat(orderItemRepository.findAllByOrderId(order.getId())).isEmpty();
    }

    @Test
    void shouldReturnOnlyItemsBelongingToRequestedOrder() {
        Product product = persistProduct("Shirt", "Clothing");
        ProductVariation variation = persistVariation(product, Size.M);
        Order order = persistOrder("customer@mail.com", OrderStatus.PAID, LocalDateTime.now());
        Order otherOrder = persistOrder("other@mail.com", OrderStatus.PAID, LocalDateTime.now());
        OrderItem expected = persistItem(order, product, variation, 1, "20.00");
        persistItem(otherOrder, product, variation, 2, "40.00");
        flushAndClear();

        assertThat(orderItemRepository.findAllByOrderId(order.getId()))
                .extracting(OrderItem::getId).containsExactly(expected.getId());
    }

    @Test
    void shouldReturnMultipleItemsForSameOrder() {
        Product shirt = persistProduct("Shirt", "Clothing");
        Product pants = persistProduct("Pants", "Bottoms");
        ProductVariation shirtVariation = persistVariation(shirt, Size.M);
        ProductVariation pantsVariation = persistVariation(pants, Size.G);
        Order order = persistOrder("customer@mail.com", OrderStatus.PAID, LocalDateTime.now());
        OrderItem first = persistItem(order, shirt, shirtVariation, 1, "20.00");
        OrderItem second = persistItem(order, pants, pantsVariation, 2, "60.00");
        flushAndClear();

        assertThat(orderItemRepository.findAllByOrderId(order.getId()))
                .extracting(OrderItem::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void shouldReturnTrueWhenCustomerBoughtProductInRequestedStatus() {
        Customer customer = persistCustomer("customer@mail.com");
        Product product = persistProduct("Shirt", "Clothing");
        Order order = persistOrder(customer, OrderStatus.PAID, LocalDateTime.now());
        persistItem(order, product, persistVariation(product, Size.M), 1, "20.00");
        flushAndClear();

        assertThat(orderItemRepository.existsByOrderCustomerIdAndProductIdAndOrderStatusIn(
                customer.getId(), product.getId(), List.of(OrderStatus.PAID, OrderStatus.DELIVERED))).isTrue();
    }

    @Test
    void shouldReturnFalseWhenProductWasBoughtInExcludedStatus() {
        Customer customer = persistCustomer("customer@mail.com");
        Product product = persistProduct("Shirt", "Clothing");
        Order order = persistOrder(customer, OrderStatus.CANCELLED, LocalDateTime.now());
        persistItem(order, product, persistVariation(product, Size.M), 1, "20.00");
        flushAndClear();

        assertThat(orderItemRepository.existsByOrderCustomerIdAndProductIdAndOrderStatusIn(
                customer.getId(), product.getId(), List.of(OrderStatus.PAID, OrderStatus.DELIVERED))).isFalse();
    }

    @Test
    void shouldReturnDistinctPurchasedVariationsForCustomerAndProduct() {
        Customer customer = persistCustomer("customer@mail.com");
        Product product = persistProduct("Shirt", "Clothing");
        ProductVariation medium = persistVariation(product, Size.M);
        ProductVariation large = persistVariation(product, Size.G);
        Order first = persistOrder(customer, OrderStatus.PAID, LocalDateTime.now());
        Order second = persistOrder(customer, OrderStatus.DELIVERED, LocalDateTime.now());
        persistItem(first, product, medium, 1, "20.00");
        persistItem(second, product, medium, 1, "20.00");
        persistItem(second, product, large, 1, "20.00");
        flushAndClear();

        assertThat(orderItemRepository.findPurchasedVariationsByCustomerAndProduct(
                customer.getId(), product.getId(), List.of(OrderStatus.PAID, OrderStatus.DELIVERED)))
                .extracting(ProductVariation::getId).containsExactlyInAnyOrder(medium.getId(), large.getId());
    }

    @Test
    void shouldReturnEmptyWhenCustomerHasNotPurchasedProductInRequestedStatuses() {
        Customer customer = persistCustomer("customer@mail.com");
        Product product = persistProduct("Shirt", "Clothing");
        Order order = persistOrder(customer, OrderStatus.CANCELLED, LocalDateTime.now());
        persistItem(order, product, persistVariation(product, Size.M), 1, "20.00");
        flushAndClear();

        assertThat(orderItemRepository.findPurchasedVariationsByCustomerAndProduct(
                customer.getId(), product.getId(), List.of(OrderStatus.PAID, OrderStatus.DELIVERED))).isEmpty();
    }

    @Test
    void shouldAggregateRevenueAndQuantityByCategoryExcludingCancelledOrders() {
        Category clothing = persistCategory("Clothing");
        Product shirt = persistProduct("Shirt", clothing);
        Product pants = persistProduct("Pants", clothing);
        ProductVariation shirtVariation = persistVariation(shirt, Size.M);
        ProductVariation pantsVariation = persistVariation(pants, Size.G);
        Order paid = persistOrder("customer@mail.com", OrderStatus.PAID, LocalDateTime.of(2026, 2, 10, 12, 0));
        Order cancelled = persistOrder("other@mail.com", OrderStatus.CANCELLED, LocalDateTime.of(2026, 2, 11, 12, 0));
        persistItem(paid, shirt, shirtVariation, 2, "25.00");
        persistItem(paid, pants, pantsVariation, 1, "40.00");
        persistItem(cancelled, shirt, shirtVariation, 10, "25.00");
        flushAndClear();

        List<Object[]> result = orderItemRepository.findRevenueByCategory(
                LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 3, 1, 0, 0),
                List.of(OrderStatus.CANCELLED));

        assertThat(result).singleElement().satisfies(row -> {
            assertThat(row[0]).isEqualTo("Clothing");
            assertThat(row[1]).isEqualTo(new BigDecimal("90.00"));
            assertThat(row[2]).isEqualTo(3L);
        });
    }

    @Test
    void shouldReturnEmptyRevenueWhenNoItemsMatchDateRange() {
        Product product = persistProduct("Shirt", "Clothing");
        ProductVariation variation = persistVariation(product, Size.M);
        Order order = persistOrder("customer@mail.com", OrderStatus.PAID, LocalDateTime.of(2026, 1, 1, 12, 0));
        persistItem(order, product, variation, 1, "20.00");
        flushAndClear();

        assertThat(orderItemRepository.findRevenueByCategory(
                LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 3, 1, 0, 0),
                List.of(OrderStatus.CANCELLED))).isEmpty();
    }

    private Customer persistCustomer(String email) {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail(email);
        customer.setPassword("password");
        customer.setTermsAndConditionsAcceptedAt(LocalDateTime.now());
        entityManager.persist(customer);
        return customer;
    }

    private Order persistOrder(String email, OrderStatus status, LocalDateTime createdAt) {
        return persistOrder(persistCustomer(email), status, createdAt);
    }

    private Order persistOrder(Customer customer, OrderStatus status, LocalDateTime createdAt) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(status);
        order.setCreatedAt(createdAt);
        order.setProductsValue(new BigDecimal("100.00"));
        order.setDiscountValue(BigDecimal.ZERO);
        order.setShippingPrice(new BigDecimal("10.00"));
        entityManager.persist(order);
        return order;
    }

    private Category persistCategory(String name) {
        Category category = new Category();
        category.setName(name);
        entityManager.persist(category);
        return category;
    }

    private Product persistProduct(String name, String categoryName) {
        return persistProduct(name, persistCategory(categoryName));
    }

    private Product persistProduct(String name, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(new BigDecimal("20.00"));
        entityManager.persist(product);
        return product;
    }

    private ProductVariation persistVariation(Product product, Size size) {
        ProductVariation variation = new ProductVariation();
        variation.setProduct(product);
        variation.setColor("Black");
        variation.setSize(size);
        variation.setStock(10);
        entityManager.persist(variation);
        return variation;
    }

    private OrderItem persistItem(Order order, Product product, ProductVariation variation,
                                  int quantity, String subtotal) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setProductVariation(variation);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(subtotal));
        item.setSubtotal(new BigDecimal(subtotal).multiply(BigDecimal.valueOf(quantity)));
        entityManager.persist(item);
        return item;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}