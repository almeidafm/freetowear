package com.freetowear.controller.api.customer;

import com.freetowear.enums.OrderStatus;
import com.freetowear.dto.request.order.AddItemToOrderRequest;
import com.freetowear.dto.request.order.CreateOrderRequest;
import com.freetowear.dto.request.order.FinishOrderRequest;
import com.freetowear.dto.response.order.OrderResponse;
import com.freetowear.dto.response.order.OrderTrackingResponse;
import com.freetowear.infra.security.CustomerDetails;
import com.freetowear.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

/*
 * OrderController — manages customer orders.
 * POST   /order ✔
 * POST   /order/{id}/item ✔
 * POST   /order/{id}/cancel ✔
 * POST   /order/{id}/finish ✔
 * GET    /order ✔
 * GET    /order/{id} ✔
 * GET    /order/{id}/tracking ✔
 * PATCH  /order/{id} ⏳
 * PATCH  /order/{id}/item/{idItem} ⏳
 * DELETE /order/{id} ⏳
 * DELETE /order/{id}/item/{idItem} ⏳
 * */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public String createOrder(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @RequestParam String idAddress,
            @RequestParam(required = false) String idCoupon
    ) {
        String idCustomer = customerDetails.getId();
        orderService.createOrder(new CreateOrderRequest(idCustomer, idAddress, idCoupon));
        return "redirect:/";
    }

    @PostMapping("/item")
    public String addItem(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @RequestParam String idProduct,
            @RequestParam String idVariation,
            @RequestParam Integer quantity,
            @RequestParam(required = false) MultipartFile customerCustomization,
            @RequestParam(required = false) String description
    ) {
        try {
            orderService.addItem(customerDetails.getId(), new AddItemToOrderRequest(idProduct, idVariation, quantity, customerCustomization, description));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/";
    }
    @PostMapping("/{id}/finish")
    public String finishOrder(
            @PathVariable String id,
            @Valid @ModelAttribute FinishOrderRequest request
    ) {
        orderService.finishOrder(id, request);
        return "redirect:/";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public OrderResponse getOrderById(@PathVariable String id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/{id}/tracking")
    @ResponseBody
    public OrderTrackingResponse getOrderTracking(@PathVariable String id) {
        return orderService.getOrderTracking(id);
    }

    @PostMapping("/cancel")
    public String cancelOrder(
            @AuthenticationPrincipal CustomerDetails customerDetails
    ) {
        orderService.cancelOrder(customerDetails.getId());
        return "redirect:/";
    }

    @GetMapping("/cart")
    @ResponseBody
    public OrderResponse getCart(
            @AuthenticationPrincipal CustomerDetails customerDetails
    ) {
        return orderService.getCart(customerDetails.getId());
    }

    @GetMapping
    @ResponseBody
    public List<OrderResponse> getOrders(
            @AuthenticationPrincipal CustomerDetails customerDetails
    ) {
        return orderService.getOrders(customerDetails.getId());
    }
}