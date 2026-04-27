package com.freetowear.freetowear.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cupom", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo")
})
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_cupom;

    @Column(nullable = false, length = 20, unique = true)
    private String codigo;

    @Column(length = 200)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDesconto tipo_desconto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor_desconto;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor_minimo_pedido;

    @Column(nullable = false)
    private LocalDate data_inicio;

    @Column(nullable = false)
    private LocalDate data_fim;

    @Column(nullable = false)
    private Boolean ativo = true;

    // =====================
    // GETTERS E SETTERS
    // =====================

    public Integer getId_cupom() {
        return id_cupom;
    }

    public void setId_cupom(Integer id_cupom) {
        this.id_cupom = id_cupom;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TipoDesconto getTipo_desconto() {
        return tipo_desconto;
    }

    public void setTipo_desconto(TipoDesconto tipo_desconto) {
        this.tipo_desconto = tipo_desconto;
    }

    public BigDecimal getValor_desconto() {
        return valor_desconto;
    }

    public void setValor_desconto(BigDecimal valor_desconto) {
        this.valor_desconto = valor_desconto;
    }

    public BigDecimal getValor_minimo_pedido() {
        return valor_minimo_pedido;
    }

    public void setValor_minimo_pedido(BigDecimal valor_minimo_pedido) {
        this.valor_minimo_pedido = valor_minimo_pedido;
    }

    public LocalDate getData_inicio() {
        return data_inicio;
    }

    public void setData_inicio(LocalDate data_inicio) {
        this.data_inicio = data_inicio;
    }

    public LocalDate getData_fim() {
        return data_fim;
    }

    public void setData_fim(LocalDate data_fim) {
        this.data_fim = data_fim;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    // =====================
    // ENUM
    // =====================

    public enum TipoDesconto {
        percentual,
        fixo
    }
}