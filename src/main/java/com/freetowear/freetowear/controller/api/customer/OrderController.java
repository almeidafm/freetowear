package com.freetowear.freetowear.controller.api.customer;

import com.freetowear.freetowear.entity.*;
import com.freetowear.freetowear.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

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
 * DELETE  /order/{id} ⏳
 * DELETE  /order/{id}/item/{idItem} ⏳
 * */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProductVariationRepository variationRepository;

    @PostMapping
    public String createOrder(
            @RequestParam Integer idCustomer,
            @RequestParam Integer idAddress,
            @RequestParam(required = false) Integer idCoupon,
            Model model
    ) {

        Optional<Customer> customerOpt = customerRepository.findById(idCustomer);
        Optional<Address> addressOpt = addressRepository.findById(idAddress);

        if (customerOpt.isEmpty() || addressOpt.isEmpty()) {
            model.addAttribute("erro", "Cliente ou endereço não encontrado");
            return "erro";
        }

        Order order = new Order();
        order.setCustomer(customerOpt.get());
        order.setDeliveryAddress(addressOpt.get());

        if (idCoupon != null) {
            couponRepository.findById(idCoupon).ifPresent(order::setCoupon);
        }

        order.setProductsValue(new BigDecimal("100.00"));
        order.setShippingPrice(new BigDecimal("20.00"));

        if (order.getCoupon() != null) {
            order.setDiscountValue(new BigDecimal("10.00"));
        }

        orderRepository.save(order);

        return "redirect:/";
    }

    @PostMapping("/{id}/item")
    public String addItem(
            @PathVariable Integer id,
            @RequestParam Integer idProduct,
            @RequestParam Integer idVariation,
            @RequestParam Integer quantity
    ) {

        Optional<Order> orderOpt = orderRepository.findById(id);
        Optional<Product> productOpt = productRepository.findById(idProduct);
        Optional<ProductVariation> variacaoOpt = variationRepository.findById(idVariation);

        if (orderOpt.isEmpty() || productOpt.isEmpty() || variacaoOpt.isEmpty()) {
            return "erro";
        }

        OrderItem item = new OrderItem();
        item.setOrder(orderOpt.get());
        item.setProduct(productOpt.get());
        item.setProductVariation(variacaoOpt.get());
        item.setQuantity(quantity);

        BigDecimal price = productOpt.get().getPrice();
        item.setUnitPrice(price);

        orderItemRepository.save(item);

        return "redirect:/";
    }

    @PostMapping("/{id}/finish")
    public String finishOrder(
            @PathVariable Integer id,
            @RequestParam Payment.PaymentMethod method,
            @RequestParam(required = false) Integer installment
    ) {

        Optional<Order> orderOpt = orderRepository.findById(id);

        if (orderOpt.isEmpty()) {
            return "erro";
        }

        Order order = orderOpt.get();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(method);
        payment.setAmountPaid(order.getTotalValue());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaidAt(LocalDateTime.now());

        if (installment != null) {
            payment.setInstallments(installment);
        }

        paymentRepository.save(payment);

        order.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(order);

        return "redirect:/";
    }
}