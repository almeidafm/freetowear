package com.freetowear.controller.api.customer;

import com.freetowear.dto.request.order.*;
import com.freetowear.dto.response.order.*;
import com.freetowear.dto.response.coupon.*;
import com.freetowear.enums.OrderStatus;
import com.freetowear.enums.OrderTrackingStatus;
import com.freetowear.exception.BusinessException;
import com.freetowear.exception.ResourceNotFoundException;
import com.freetowear.infra.security.CustomerDetails;
import com.freetowear.repository.OrderRepository;
import com.freetowear.service.OrderService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/item")
    public ResponseEntity<?> addItem(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @RequestParam String idProduct,
            @RequestParam String idVariation,
            @RequestParam Integer quantity,
            @RequestParam(required = false) MultipartFile customerCustomization,
            @RequestParam(required = false) String description
    ) throws IOException {
        orderService.addItem(customerDetails.getId(), new AddItemToCartRequest(idProduct, idVariation, quantity, customerCustomization, description));
        return ResponseEntity.ok(Map.of("message", "Item added to cart successfully"));
    }

    @PostMapping("/current/finish")
    public ResponseEntity<?> finishCurrentOrder(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @Valid @ModelAttribute FinishOrderRequest request
    ) {
        orderRepository
                .findByCustomerIdAndStatus(customerDetails.getId(), OrderStatus.CART)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        orderService.finishOrder(customerDetails.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Order finished successfully"));
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

    @PostMapping("/{id}/tracking")
    public String updateOrderTracking(
            @PathVariable String id,
            @RequestParam OrderTrackingStatus trackingStatus,
            @RequestParam(required = false) String trackingCode
    ) {
        orderService.updateOrderTracking(id, trackingStatus, trackingCode);
        return "redirect:/admin";
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelOrder(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @RequestParam String orderId
    ) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));
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
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
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

    @GetMapping({"/coupon", "/coupon/{code}"})
    @ResponseBody
    public ResponseEntity<?> validateCoupon(
            @AuthenticationPrincipal CustomerDetails customerDetails,
            @PathVariable(required = false) String code,
            @RequestParam(name = "code", required = false) String codeParam
    ) {
        String couponCode = code != null ? code : codeParam;
        if (couponCode == null || couponCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Coupon code is required"));
        }
        CouponResponse coupon = orderService.validateCoupon(customerDetails.getId(), couponCode);
        return ResponseEntity.ok(coupon);
    }
    @PostMapping("/pay")
    public String payOrder(
            @AuthenticationPrincipal CustomerDetails customerDetails
    ) {
        try {
            orderService.payOrder(customerDetails.getId());
            return "redirect:/order-success";
        } catch (ResourceNotFoundException | BusinessException e) {
            return "redirect:/payment?error=no-active-order";
        }
    }
}