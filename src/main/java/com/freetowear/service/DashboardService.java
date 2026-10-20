package com.freetowear.service;

import com.freetowear.dto.response.dashboard.*;
import com.freetowear.entity.Order;
import com.freetowear.entity.OrderTracking;
import com.freetowear.entity.Review;
import com.freetowear.enums.OrderStatus;
import com.freetowear.enums.OrderTrackingStatus;
import com.freetowear.repository.*;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderTrackingRepository orderTrackingRepository;

    private static final Set<OrderStatus> EXCLUDED_STATUSES = Set.of(
            OrderStatus.CART,
            OrderStatus.ABANDONED,
            OrderStatus.CANCELLED
    );

    public DashboardService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            ReviewRepository reviewRepository,
            OrderItemRepository orderItemRepository,
            OrderTrackingRepository orderTrackingRepository
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.reviewRepository = reviewRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderTrackingRepository = orderTrackingRepository;
    }

    public DashboardResponse getDashboardMetrics(int days) {
        int windowDays = days > 0 ? days : 30;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentStart = now.minusDays(windowDays);
        LocalDateTime currentEnd = now;
        LocalDateTime prevStart = currentStart.minusDays(windowDays);
        LocalDateTime prevEnd = currentStart;

        List<Order> currentOrders = orderRepository.findByCreatedAtBetween(currentStart, currentEnd);
        List<Order> prevOrders = orderRepository.findByCreatedAtBetween(prevStart, prevEnd);

        List<Order> validCurrent = currentOrders.stream()
                .filter(o -> !EXCLUDED_STATUSES.contains(o.getStatus()))
                .toList();
        List<Order> validPrev = prevOrders.stream()
                .filter(o -> !EXCLUDED_STATUSES.contains(o.getStatus()))
                .toList();

        // 1. Gross Revenue
        BigDecimal grossRevCurrent = sum(validCurrent, Order::getTotalValue);
        BigDecimal grossRevPrev = sum(validPrev, Order::getTotalValue);
        KpiMetricResponse grossRevenueKpi = KpiMetricResponse.of(grossRevCurrent, grossRevPrev);

        // 2. Net Revenue
        BigDecimal netRevCurrent = validCurrent.stream()
                .map(o -> safe(o.getProductsValue()).subtract(safe(o.getDiscountValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netRevPrev = validPrev.stream()
                .map(o -> safe(o.getProductsValue()).subtract(safe(o.getDiscountValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        KpiMetricResponse netRevenueKpi = KpiMetricResponse.of(netRevCurrent, netRevPrev);

        // 3. Orders Count
        long ordersCountCurrent = validCurrent.size();
        long ordersCountPrev = validPrev.size();
        KpiMetricResponse ordersCountKpi = KpiMetricResponse.of(ordersCountCurrent, ordersCountPrev);

        // 4. AOV
        BigDecimal aovCurrent = ordersCountCurrent > 0
                ? grossRevCurrent.divide(BigDecimal.valueOf(ordersCountCurrent), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal aovPrev = ordersCountPrev > 0
                ? grossRevPrev.divide(BigDecimal.valueOf(ordersCountPrev), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        KpiMetricResponse aovKpi = KpiMetricResponse.of(aovCurrent, aovPrev);

        // 5. New Customers
        long newCustomersCurrent = customerRepository.countByCreatedAtBetween(currentStart, currentEnd);
        long newCustomersPrev = customerRepository.countByCreatedAtBetween(prevStart, prevEnd);
        KpiMetricResponse newCustomersKpi = KpiMetricResponse.of(newCustomersCurrent, newCustomersPrev);

        // 6. Repeat Purchase Rate
        Double repeatRateCurrent = calculateRepeatRate(validCurrent);
        Double repeatRatePrev = calculateRepeatRate(validPrev);
        KpiMetricResponse repeatPurchaseRateKpi = KpiMetricResponse.of(repeatRateCurrent, repeatRatePrev);

        // 7. Cancellation Rate
        Double cancellationRateCurrent = calculateCancellationRate(currentOrders);
        Double cancellationRatePrev = calculateCancellationRate(prevOrders);
        KpiMetricResponse cancellationRateKpi = KpiMetricResponse.of(cancellationRateCurrent, cancellationRatePrev);

        // 8. Avg Delivery Time (Days)
        Double avgDeliveryDaysCurrent = calculateAvgDeliveryDays(currentStart, currentEnd);
        Double avgDeliveryDaysPrev = calculateAvgDeliveryDays(prevStart, prevEnd);
        KpiMetricResponse avgDeliveryTimeKpi = KpiMetricResponse.of(avgDeliveryDaysCurrent, avgDeliveryDaysPrev);

        // 9. Avg Rating
        List<Review> currentReviews = reviewRepository.findByCreatedAtBetween(currentStart, currentEnd);
        List<Review> prevReviews = reviewRepository.findByCreatedAtBetween(prevStart, prevEnd);
        Double avgRatingCurrent = currentReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        Double avgRatingPrev = prevReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        KpiMetricResponse avgRatingKpi = KpiMetricResponse.of(avgRatingCurrent, avgRatingPrev);

        DashboardKpisResponse kpis = DashboardKpisResponse.builder()
                .grossRevenue(grossRevenueKpi)
                .netRevenue(netRevenueKpi)
                .ordersCount(ordersCountKpi)
                .averageOrderValue(aovKpi)
                .newCustomers(newCustomersKpi)
                .repeatPurchaseRate(repeatPurchaseRateKpi)
                .cancellationRate(cancellationRateKpi)
                .averageDeliveryTimeDays(avgDeliveryTimeKpi)
                .averageRating(avgRatingKpi)
                .build();

        // 10. Revenue by Category
        List<CategoryRevenueResponse> categoryRevenue = calculateCategoryRevenue(currentStart, currentEnd);

        // 11. Revenue by State
        List<StateRevenueResponse> stateRevenue = calculateStateRevenue(validCurrent);

        // 12. Freight Summary
        FreightSummaryResponse freightSummary = calculateFreightSummary(validCurrent, grossRevCurrent);

        // 12-Month Trends
        MonthlyTrendResponse revenue12Months = calculate12MonthRevenue();
        MonthlyTrendResponse rating12Months = calculate12MonthRating();

        return DashboardResponse.builder()
                .periodDays(windowDays)
                .kpis(kpis)
                .revenue12Months(revenue12Months)
                .rating12Months(rating12Months)
                .categoryRevenue(categoryRevenue)
                .stateRevenue(stateRevenue)
                .freightSummary(freightSummary)
                .build();
    }

    private Double calculateRepeatRate(List<Order> orders) {
        Map<String, Long> customerOrders = orders.stream()
                .filter(o -> o.getCustomer() != null && o.getCustomer().getId() != null)
                .collect(Collectors.groupingBy(o -> o.getCustomer().getId(), Collectors.counting()));
        if (customerOrders.isEmpty()) {
            return 0.0;
        }
        long repeatCount = customerOrders.values().stream().filter(count -> count >= 2).count();
        return (repeatCount * 100.0) / customerOrders.size();
    }

    private Double calculateCancellationRate(List<Order> orders) {
        List<Order> placedOrders = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CART && o.getStatus() != OrderStatus.ABANDONED)
                .toList();
        if (placedOrders.isEmpty()) {
            return 0.0;
        }
        long cancelled = placedOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .count();
        return (cancelled * 100.0) / placedOrders.size();
    }

    private Double calculateAvgDeliveryDays(LocalDateTime start, LocalDateTime end) {
        List<OrderTracking> deliveryEvents = orderTrackingRepository
                .findByTrackingStatusAndOccurredAtBetween(OrderTrackingStatus.DELIVERED, start, end);
        if (deliveryEvents.isEmpty()) {
            return 0.0;
        }
        double totalDays = 0.0;
        int count = 0;
        for (OrderTracking event : deliveryEvents) {
            if (event.getOrder() != null && event.getOrder().getCreatedAt() != null && event.getOccurredAt() != null) {
                long hours = Duration.between(event.getOrder().getCreatedAt(), event.getOccurredAt()).toHours();
                totalDays += (hours / 24.0);
                count++;
            }
        }
        return count > 0 ? (totalDays / count) : 0.0;
    }

    private List<CategoryRevenueResponse> calculateCategoryRevenue(LocalDateTime start, LocalDateTime end) {
        List<Object[]> raw = orderItemRepository.findRevenueByCategory(start, end, EXCLUDED_STATUSES);
        List<CategoryRevenueResponse> result = new ArrayList<>();
        for (Object[] row : raw) {
            String category = row[0] != null ? row[0].toString() : "Geral";
            BigDecimal rev = row[1] instanceof BigDecimal b ? b : (row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO);
            Long qty = row[2] instanceof Number n ? n.longValue() : 0L;
            result.add(new CategoryRevenueResponse(category, rev, qty));
        }
        return result;
    }

    private List<StateRevenueResponse> calculateStateRevenue(List<Order> orders) {
        Map<String, List<Order>> ordersByState = orders.stream()
                .collect(Collectors.groupingBy(o -> {
                    if (o.getDeliveryAddress() != null && o.getDeliveryAddress().getState() != null) {
                        return o.getDeliveryAddress().getState().name();
                    }
                    return "Outros";
                }));

        return ordersByState.entrySet().stream()
                .map(entry -> {
                    BigDecimal rev = sum(entry.getValue(), Order::getTotalValue);
                    return new StateRevenueResponse(entry.getKey(), rev, (long) entry.getValue().size());
                })
                .sorted(Comparator.comparing(StateRevenueResponse::getRevenue).reversed())
                .toList();
    }

    private FreightSummaryResponse calculateFreightSummary(List<Order> orders, BigDecimal grossRevenue) {
        if (orders.isEmpty()) {
            return new FreightSummaryResponse(BigDecimal.ZERO, 0.0, 0.0);
        }
        BigDecimal totalFreight = sum(orders, Order::getShippingPrice);
        BigDecimal avgFreight = totalFreight.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);

        Double freightRatio = grossRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalFreight.divide(grossRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        long freeShippingCount = orders.stream()
                .filter(o -> o.getShippingPrice() == null || o.getShippingPrice().compareTo(BigDecimal.ZERO) == 0)
                .count();
        Double freeShippingPct = (freeShippingCount * 100.0) / orders.size();

        return new FreightSummaryResponse(avgFreight, freightRatio, freeShippingPct);
    }

    private MonthlyTrendResponse calculate12MonthRevenue() {
        YearMonth currentYearMonth = YearMonth.now();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM/yy", Locale.forLanguageTag("pt-BR"));

        LocalDateTime start12m = currentYearMonth.minusMonths(11).atDay(1).atStartOfDay();
        List<Order> orders = orderRepository.findByCreatedAtGreaterThanEqual(start12m).stream()
                .filter(o -> !EXCLUDED_STATUSES.contains(o.getStatus()))
                .toList();

        for (int i = 11; i >= 0; i--) {
            YearMonth targetMonth = currentYearMonth.minusMonths(i);
            labels.add(targetMonth.format(formatter));
            BigDecimal monthRev = orders.stream()
                    .filter(o -> YearMonth.from(o.getCreatedAt()).equals(targetMonth))
                    .map(o -> safe(o.getTotalValue()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            data.add(monthRev);
        }
        return new MonthlyTrendResponse(labels, data);
    }

    private MonthlyTrendResponse calculate12MonthRating() {
        YearMonth currentYearMonth = YearMonth.now();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM/yy", Locale.forLanguageTag("pt-BR"));

        LocalDateTime start12m = currentYearMonth.minusMonths(11).atDay(1).atStartOfDay();
        List<Review> reviews = reviewRepository.findByCreatedAtGreaterThanEqual(start12m);

        for (int i = 11; i >= 0; i--) {
            YearMonth targetMonth = currentYearMonth.minusMonths(i);
            labels.add(targetMonth.format(formatter));
            Double avg = reviews.stream()
                    .filter(r -> YearMonth.from(r.getCreatedAt()).equals(targetMonth))
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            data.add(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        }
        return new MonthlyTrendResponse(labels, data);
    }

    private BigDecimal sum(List<Order> orders, java.util.function.Function<Order, BigDecimal> extractor) {
        return orders.stream()
                .map(o -> safe(extractor.apply(o)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
