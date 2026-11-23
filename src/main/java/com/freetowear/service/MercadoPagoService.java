package com.freetowear.service;

import com.freetowear.entity.Order;
import com.freetowear.entity.Payment;
import com.freetowear.enums.OrderStatus;
import com.freetowear.enums.PaymentStatus;
import com.freetowear.exception.BusinessException;
import com.freetowear.repository.OrderItemRepository;
import com.freetowear.repository.OrderRepository;
import com.freetowear.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MercadoPagoService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final RestClient client;

    @Value("${mercadopago.access-token:}") private String accessToken;
    @Value("${mercadopago.base-url:http://localhost:8080}") private String baseUrl;
    @Value("${mercadopago.sandbox:true}") private boolean sandbox;

    public MercadoPagoService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                              PaymentRepository paymentRepository, RestClient.Builder builder) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.client = builder.baseUrl("https://api.mercadopago.com").build();
    }

    @Transactional(readOnly = true)
    public String createCheckout(String orderId, String customerId) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException("Mercado Pago nao configurado: defina MERCADOPAGO_ACCESS_TOKEN");
        }
        Order order = orderRepository.findById(orderId)
                .filter(value -> value.getCustomer().getId().equals(customerId))
                .orElseThrow(() -> new BusinessException("Pedido nao encontrado"));
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("Pagamento do pedido nao encontrado"));

        List<Map<String, Object>> items = orderItemRepository.findAllByOrderId(orderId).stream().map(item -> {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("id", item.getId());
            value.put("title", item.getProduct() != null ? item.getProduct().getName() : "Camiseta personalizada");
            value.put("quantity", item.getQuantity());
            value.put("unit_price", item.getUnitPrice());
            value.put("currency_id", "BRL");
            return value;
        }).toList();
        if (order.getShippingPrice() != null && order.getShippingPrice().signum() > 0) {
            Map<String, Object> shipping = new LinkedHashMap<>();
            shipping.put("id", "shipping-" + orderId);
            shipping.put("title", "Entrega"); shipping.put("quantity", 1);
            shipping.put("unit_price", order.getShippingPrice()); shipping.put("currency_id", "BRL");
            items = new java.util.ArrayList<>(items); items.add(shipping);
        }

        String reference = orderId;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("external_reference", reference);
        body.put("items", items);
        String publicBaseUrl = baseUrl.replaceAll("/+$", "");
        body.put("back_urls", Map.of(
                "success", publicBaseUrl + "/payment/mercadopago/return",
                "pending", publicBaseUrl + "/payment/mercadopago/return",
                "failure", publicBaseUrl + "/payment/mercadopago/return"));
        body.put("auto_return", "approved");
        Map<String, Object> response = client.post().uri("/checkout/preferences")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
        String preferenceId = String.valueOf(response.get("id"));
        payment.setProviderReference(preferenceId);
        paymentRepository.save(payment);
        // Mantemos o fluxo explicitamente no Sandbox para impedir qualquer cobranca real.
        Object url = sandbox ? response.get("sandbox_init_point") : response.get("init_point");
        if (url == null) throw new BusinessException("Mercado Pago nao retornou a URL de checkout");
        return url.toString();
    }

    @Transactional
    public void processReturn(String paymentId, String status, String externalReference) {
        if (paymentId == null || externalReference == null) return;
        Order order = orderRepository.findById(externalReference).orElse(null);
        if (order == null) return;
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (payment == null) return;
        // O status da query e apenas indicativo; a confirmacao definitiva deve vir do webhook/API.
        payment.setTransactionCode(paymentId);
        if ("approved".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.APPROVED);
            order.setStatus(OrderStatus.PAID);
        } else if ("rejected".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.REJECTED);
        }
        paymentRepository.save(payment); orderRepository.save(order);
    }
}
