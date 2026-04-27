package com.freetowear.freetowear.model;

import jakarta.persistence.*;

@Entity
@Table(name = "produto_variacao",
        indexes = {
                @Index(name = "idx_cor", columnList = "cor"),
                @Index(name = "idx_tamanho", columnList = "tamanho")
        })
public class ProdutoVariacao {

    public enum Tamanho {
        PP, P, M, G, GG, XG
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_variacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_produto")
    private Produto produto;

    @Column(nullable = false, length = 50)
    private String cor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tamanho tamanho;

    @Column(nullable = false)
    private Integer estoque = 0;

    // getters e setters

    public Integer getId_variacao() {
        return id_variacao;
    }

    public void setId_variacao(Integer id_variacao) {
        this.id_variacao = id_variacao;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public Tamanho getTamanho() {
        return tamanho;
    }

    public void setTamanho(Tamanho tamanho) {
        this.tamanho = tamanho;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }
    
}