package com.freetowear.controller.api.admin;

import com.freetowear.dto.response.order.OrderResponse;
import com.freetowear.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/paid")
    @ResponseBody
    public List<OrderResponse> getPaidOrders() {
        return orderService.getPaidOrdersForAdmin();
    }

    @GetMapping("/data/{id}")
    @ResponseBody
    public OrderResponse getOrder(@PathVariable String id) {
        return orderService.getAdminOrderById(id);
    }
}