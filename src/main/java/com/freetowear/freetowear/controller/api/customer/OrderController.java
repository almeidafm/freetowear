package com.freetowear.freetowear.controller.api.customer;

import com.freetowear.freetowear.dto.request.order.AddItemToOrderRequest;
import com.freetowear.freetowear.dto.request.order.CreateOrderRequest;
import com.freetowear.freetowear.dto.request.order.FinishOrderRequest;
import com.freetowear.freetowear.entity.Payment;
import com.freetowear.freetowear.enums.PaymentMethod;
import com.freetowear.freetowear.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/*
 * OrderController — manages customer orders.
 * POST   /order ✔
 * POST   /order/{id}/item ✔
 * POST   /order/{id}/cancel ⏳
 * POST   /order/{id}/finish ✔
 * GET    /order ⏳
 * GET    /order/{id} ⏳
 * GET    /order/{id}/tracking ⏳
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
            @RequestParam Integer idCustomer,
            @RequestParam Integer idAddress,
            @RequestParam(required = false) Integer idCoupon
    ) {
        orderService.createOrder(new CreateOrderRequest(idCustomer, idAddress, idCoupon));
        return "redirect:/";
    }

    @PostMapping("/{id}/item")
    public String addItem(
            @PathVariable Integer id,
            @RequestParam Integer idProduct,
            @RequestParam Integer idVariation,
            @RequestParam Integer quantity
    ) {
        orderService.addItem(id, new AddItemToOrderRequest(idProduct, idVariation, quantity));
        return "redirect:/";
    }

    @PostMapping("/{id}/finish")
    public String finishOrder(
            @PathVariable Integer id,
            @Valid @ModelAttribute FinishOrderRequest request
    ) {
        orderService.finishOrder(id, request);
        return "redirect:/";
    }
}