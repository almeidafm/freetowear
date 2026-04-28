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
    private PedidoRepository pedidoRepository;

    @Autowired
    private  ProdutoRepository produtoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private CupomRepository cupomRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @PostMapping
    public String createOrder(
            @RequestParam Integer idCliente,
            @RequestParam Integer idEndereco,
            @RequestParam(required = false) Integer idCupom,
            Model model
    ) {

        Optional<Cliente> clienteOpt = clienteRepository.findById(idCliente);
        Optional<Endereco> enderecoOpt = enderecoRepository.findById(idEndereco);

        if (clienteOpt.isEmpty() || enderecoOpt.isEmpty()) {
            model.addAttribute("erro", "Cliente ou endereço não encontrado");
            return "erro";
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(clienteOpt.get());
        pedido.setEnderecoEntrega(enderecoOpt.get());

        if (idCupom != null) {
            cupomRepository.findById(idCupom).ifPresent(pedido::setCupom);
        }

        pedido.setValor_produtos(new BigDecimal("100.00"));
        pedido.setValor_frete(new BigDecimal("20.00"));

        if (pedido.getCupom() != null) {
            pedido.setValor_desconto(new BigDecimal("10.00"));
        }

        pedidoRepository.save(pedido);

        return "redirect:/";
    }

    @PostMapping("/{id}/item")
    public String addItem(
            @PathVariable Integer id,
            @RequestParam Integer idProduto,
            @RequestParam Integer idVariacao,
            @RequestParam Integer quantidade
    ) {

        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        Optional<Produto> produtoOpt = produtoRepository.findById(idProduto);
        Optional<Produto> variacaoOpt = produtoRepository.findById(idVariacao);

        if (pedidoOpt.isEmpty() || produtoOpt.isEmpty() || variacaoOpt.isEmpty()) {
            return "erro";
        }

        ItemPedido item = new ItemPedido();
        item.setPedido(pedidoOpt.get());
        item.setProduto(produtoOpt.get());
        item.setProdutoVariacao(variacaoOpt.get());
        item.setQuantidade(quantidade);

        BigDecimal preco = produtoOpt.get().getPreco();
        item.setPreco_unitario(preco);

        itemPedidoRepository.save(item);

        return "redirect:/";
    }

    @PostMapping("/{id}/finish")
    public String finishOrder(
            @PathVariable Integer id,
            @RequestParam Pagamento.MetodoPagamento metodo,
            @RequestParam(required = false) Integer parcelas
    ) {

        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);

        if (pedidoOpt.isEmpty()) {
            return "erro";
        }

        Pedido pedido = pedidoOpt.get();

        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setMetodo(metodo);
        pagamento.setValor_pago(pedido.getValor_total());
        pagamento.setStatus(Pagamento.StatusPagamento.PENDENTE);
        pagamento.setData_pagamento(LocalDateTime.now());

        if (parcelas != null) {
            pagamento.setParcelas(parcelas);
        }

        pagamentoRepository.save(pagamento);

        pedido.setStatus(Pedido.StatusPedido.pago);
        pedidoRepository.save(pedido);

        return "redirect:/";
    }
}