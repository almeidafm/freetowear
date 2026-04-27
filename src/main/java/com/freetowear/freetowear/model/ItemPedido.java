package com.freetowear.freetowear.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "item_pedido", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_pedido", "id_produto", "id_variacao"})
})
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_variacao", nullable = false)
    private Produto produtoVariacao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco_unitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // =====================
    // GETTERS E SETTERS
    // =====================

    public Integer getId_item() {
        return id_item;
    }

    public void setId_item(Integer id_item) {
        this.id_item = id_item;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
        atualizarSubtotal();
    }

    public BigDecimal getPreco_unitario() {
        return preco_unitario;
    }

    public void setPreco_unitario(BigDecimal preco_unitario) {
        this.preco_unitario = preco_unitario;
        atualizarSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public Produto getProdutoVariacao() {
        return produtoVariacao;
    }

    public void setProdutoVariacao(Produto produtoVariacao) {
        this.produtoVariacao = produtoVariacao;
    }

    // =====================
    // MÉTODO PARA ATUALIZAR SUBTOTAL
    // =====================
    private void atualizarSubtotal() {
        if (preco_unitario != null && quantidade != null) {
            this.subtotal = preco_unitario.multiply(BigDecimal.valueOf(quantidade));
        }
    }
}