package com.freetowear.freetowear.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedido", indexes = {
        @Index(columnList = "status"),
        @Index(columnList = "data_pedido"),
        @Index(columnList = "id_cliente")
})
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco_entrega", nullable = false)
    private Endereco enderecoEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cupom")
    private Cupom cupom;

    @Column(nullable = false)
    private LocalDateTime data_pedido = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status = StatusPedido.carrinho;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor_produtos;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor_desconto = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor_frete;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor_total;

    @Column(length = 50)
    private String codigo_rastreio;

    private LocalDate previsao_entrega;

    // =====================
    // GETTERS E SETTERS
    // =====================

    public Integer getId_pedido() {
        return id_pedido;
    }

    public void setId_pedido(Integer id_pedido) {
        this.id_pedido = id_pedido;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Endereco getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(Endereco enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public Cupom getCupom() {
        return cupom;
    }

    public void setCupom(Cupom cupom) {
        this.cupom = cupom;
    }

    public LocalDateTime getData_pedido() {
        return data_pedido;
    }

    public void setData_pedido(LocalDateTime data_pedido) {
        this.data_pedido = data_pedido;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public BigDecimal getValor_produtos() {
        return valor_produtos;
    }

    public void setValor_produtos(BigDecimal valor_produtos) {
        this.valor_produtos = valor_produtos;
        calcularValorTotal();
    }

    public BigDecimal getValor_desconto() {
        return valor_desconto;
    }

    public void setValor_desconto(BigDecimal valor_desconto) {
        this.valor_desconto = valor_desconto;
        calcularValorTotal();
    }

    public BigDecimal getValor_frete() {
        return valor_frete;
    }

    public void setValor_frete(BigDecimal valor_frete) {
        this.valor_frete = valor_frete;
        calcularValorTotal();
    }

    public BigDecimal getValor_total() {
        return valor_total;
    }

    public String getCodigo_rastreio() {
        return codigo_rastreio;
    }

    public void setCodigo_rastreio(String codigo_rastreio) {
        this.codigo_rastreio = codigo_rastreio;
    }

    public LocalDate getPrevisao_entrega() {
        return previsao_entrega;
    }

    public void setPrevisao_entrega(LocalDate previsao_entrega) {
        this.previsao_entrega = previsao_entrega;
    }

    // =====================
    // METODO PARA ATUALIZAR VALOR TOTAL
    // =====================
    private void calcularValorTotal() {
        if (valor_produtos != null && valor_frete != null && valor_desconto != null) {
            this.valor_total = valor_produtos.subtract(valor_desconto).add(valor_frete);
        }
    }

    // =====================
    // CALLBACK JPA
    // =====================
    @PrePersist
    @PreUpdate
    private void calcularTotalAntesDeSalvar() {
        calcularValorTotal();
    }

    // =====================
    // ENUM
    // =====================
    public enum StatusPedido {
        carrinho,
        pendente,
        pago,
        enviado,
        entregue,
        cancelado,
        abandonado
    }
}