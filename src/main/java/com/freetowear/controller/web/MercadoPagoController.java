package com.freetowear.controller.web;

import com.freetowear.service.MercadoPagoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/payment/mercadopago")
public class MercadoPagoController {
    private final MercadoPagoService mercadoPagoService;

    public MercadoPagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    @GetMapping("/return")
    public String returned(@RequestParam(required = false) String payment_id,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) String external_reference) {
        mercadoPagoService.processReturn(payment_id, status, external_reference);
        return "redirect:/order-success";
    }
}
