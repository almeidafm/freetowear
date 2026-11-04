package com.freetowear.service;

import com.freetowear.dto.response.dashboard.DashboardResponse;
import com.freetowear.entity.Address;
import com.freetowear.entity.Customer;
import com.freetowear.entity.Order;
import com.freetowear.entity.OrderTracking;
import com.freetowear.entity.Review;
import com.freetowear.enums.OrderStatus;
import com.freetowear.enums.OrderTrackingStatus;
import com.freetowear.enums.UF;
import com.freetowear.repository.CustomerRepository;
import com.freetowear.repository.OrderItemRepository;
import com.freetowear.repository.OrderRepository;
import com.freetowear.repository.OrderTrackingRepository;
import com.freetowear.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderTrackingRepository orderTrackingRepository;

    @InjectMocks private DashboardService dashboardService;

    @Nested
    @DisplayName("getDashboardMetrics")
    class GetDashboardMetricsTests {

        @Test
        @DisplayName("Should use 30-day default period and return zero metrics when days is invalid")
        void shouldUseDefaultPeriodWhenDaysIsNotPositive() {
            stubEmptyRepositories();

            DashboardResponse response = dashboardService.getDashboardMetrics(0);

            assertThat(response.getPeriodDays()).isEqualTo(30);
            assertThat(response.getKpis().getGrossRevenue().getCurrentValue()).isEqualByComparingTo("0");
            assertThat(response.getKpis().getOrdersCount().getCurrentValue()).isEqualByComparingTo("0");
            assertThat(response.getKpis().getAverageRating().getCurrentValue()).isEqualByComparingTo("0.00");
            assertThat(response.getCategoryRevenue()).isEmpty();
            assertThat(response.getStateRevenue()).isEmpty();
            assertThat(response.getFreightSummary().getAverageFreight()).isEqualByComparingTo("0");
            assertThat(response.getRevenue12Months().getLabels()).hasSize(12);
            assertThat(response.getRevenue12Months().getData()).containsOnly(BigDecimal.ZERO);
            assertThat(response.getRating12Months().getLabels()).hasSize(12);
        }

        @Test
        @DisplayName("Should calculate KPIs, revenue breakdowns, freight, and trends")
        void shouldCalculateDashboardMetricsWhenOrdersAndReviewsExist() {
            Customer customer = new Customer();
            customer.setId("customer-1");
            Address address = new Address();
            address.setState(UF.SP);

            Order paid = anOrder(customer, address, OrderStatus.PAID,
                    new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("20.00"));
            Order delivered = anOrder(customer, address, OrderStatus.DELIVERED,
                    new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO);
            Order cancelled = anOrder(customer, address, OrderStatus.CANCELLED,
                    new BigDecimal("200.00"), BigDecimal.ZERO, new BigDecimal("10.00"));
            List<Order> currentOrders = List.of(paid, delivered, cancelled);
            Review review = new Review();
            review.setRating(4);
            review.setCreatedAt(LocalDateTime.now().minusDays(2));
            OrderTracking tracking = new OrderTracking();
            tracking.setOrder(delivered);
            tracking.setTrackingStatus(OrderTrackingStatus.DELIVERED);
            tracking.setOccurredAt(delivered.getCreatedAt().plusHours(48));

            when(orderRepository.findByCreatedAtBetween(any(), any())).thenReturn(currentOrders, List.of());
            when(orderRepository.findByCreatedAtGreaterThanEqual(any())).thenReturn(currentOrders);
            when(customerRepository.countByCreatedAtBetween(any(), any())).thenReturn(2L, 0L);
            when(reviewRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(review), List.of());
            when(reviewRepository.findByCreatedAtGreaterThanEqual(any())).thenReturn(List.of(review));
            when(orderTrackingRepository.findByTrackingStatusAndOccurredAtBetween(any(), any(), any()))
                    .thenReturn(List.of(tracking), List.of());
            when(orderItemRepository.findRevenueByCategory(any(), any(), any()))
                    .thenReturn(List.<Object[]>of(new Object[]{"Shirts", new BigDecimal("150.00"), 3L}));

            DashboardResponse response = dashboardService.getDashboardMetrics(7);

            assertThat(response.getPeriodDays()).isEqualTo(7);
            assertThat(response.getKpis().getGrossRevenue().getCurrentValue()).isEqualByComparingTo("170.00");
            assertThat(response.getKpis().getNetRevenue().getCurrentValue()).isEqualByComparingTo("160.00");
            assertThat(response.getKpis().getOrdersCount().getCurrentValue()).isEqualByComparingTo("2");
            assertThat(response.getKpis().getAverageOrderValue().getCurrentValue()).isEqualByComparingTo("85.00");
            assertThat(response.getKpis().getNewCustomers().getCurrentValue()).isEqualByComparingTo("2");
            assertThat(response.getKpis().getRepeatPurchaseRate().getCurrentValue()).isEqualByComparingTo("100.00");
            assertThat(response.getKpis().getCancellationRate().getCurrentValue()).isEqualByComparingTo("33.33");
            assertThat(response.getKpis().getAverageDeliveryTimeDays().getCurrentValue()).isEqualByComparingTo("2.00");
            assertThat(response.getKpis().getAverageRating().getCurrentValue()).isEqualByComparingTo("4.00");
            assertThat(response.getCategoryRevenue()).singleElement().satisfies(category -> {
                assertThat(category.getCategoryName()).isEqualTo("Shirts");
                assertThat(category.getRevenue()).isEqualByComparingTo("150.00");
                assertThat(category.getItemsSold()).isEqualTo(3L);
            });
            assertThat(response.getStateRevenue()).singleElement().satisfies(state -> {
                assertThat(state.getState()).isEqualTo("SP");
                assertThat(state.getRevenue()).isEqualByComparingTo("170.00");
                assertThat(state.getOrdersCount()).isEqualTo(2L);
            });
            assertThat(response.getFreightSummary().getAverageFreight()).isEqualByComparingTo("10.00");
            assertThat(response.getFreightSummary().getFreightToRevenuePercentage()).isEqualTo(11.76);
            assertThat(response.getFreightSummary().getFreeShippingPercentage()).isEqualTo(50.0);
        }

        private void stubEmptyRepositories() {
            when(orderRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());
            when(orderRepository.findByCreatedAtGreaterThanEqual(any())).thenReturn(List.of());
            when(customerRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
            when(reviewRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());
            when(reviewRepository.findByCreatedAtGreaterThanEqual(any())).thenReturn(List.of());
            when(orderTrackingRepository.findByTrackingStatusAndOccurredAtBetween(any(), any(), any()))
                    .thenReturn(List.of());
            when(orderItemRepository.findRevenueByCategory(any(), any(), any())).thenReturn(List.of());
        }

        private Order anOrder(Customer customer, Address address, OrderStatus status,
                              BigDecimal products, BigDecimal discount, BigDecimal shipping) {
            Order order = new Order();
            order.setCustomer(customer);
            order.setDeliveryAddress(address);
            order.setStatus(status);
            order.setProductsValue(products);
            order.setDiscountValue(discount);
            order.setShippingPrice(shipping);
            order.setTotalValue(products.subtract(discount).add(shipping));
            order.setCreatedAt(LocalDateTime.now().minusDays(2));
            return order;
        }
    }
}