package com.freetowear.controller.api.customer;

import com.freetowear.dto.request.order.AddItemToOrderRequest;
import com.freetowear.dto.request.order.FinishOrderRequest;
import com.freetowear.dto.response.order.OrderResponse;
import com.freetowear.dto.response.order.OrderTrackingResponse;
import com.freetowear.entity.Order;
import com.freetowear.enums.OrderStatus;
import com.freetowear.infra.security.CustomerDetails;
import com.freetowear.repository.OrderRepository;
import com.freetowear.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

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

    @PostMapping("/current/finish")
    public String finishCurrentOrder(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @Valid @ModelAttribute FinishOrderRequest request
    ) {
        Order order = orderRepository
                .findByCustomerIdAndStatus(customerDetails.getId(), OrderStatus.CART)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        orderService.finishOrder(order.getId(), request);

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
    public String cart() {
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateCartItem(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @RequestParam String idProduct,
            @RequestParam String idVariation,
            @RequestParam Integer quantity
    ) {
        orderService.updateItemQuantity(
                customerDetails.getId(),
                idProduct,
                idVariation,
                quantity
        );

        return "redirect:/cart";
    }

    @GetMapping("/current")
    @ResponseBody
    public OrderResponse getCurrentCart(
            @AuthenticationPrincipal CustomerDetails customerDetails
    ) {
        return orderService.getCart(customerDetails.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    @PostMapping("/cart/remove")
    public String removeCartItem(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @RequestParam String idProduct,
            @RequestParam String idVariation
    ) {
        orderService.removeItem(
                customerDetails.getId(),
                idProduct,
                idVariation
        );

        return "redirect:/cart";
    }

    @GetMapping
    @ResponseBody
    public List<OrderResponse> getOrders(
            @AuthenticationPrincipal CustomerDetails customerDetails
    ) {
        return orderService.getOrders(customerDetails.getId());
    }
}