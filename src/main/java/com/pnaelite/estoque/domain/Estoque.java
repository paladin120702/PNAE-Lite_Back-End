package com.pnaelite.estoque.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

/**
 * Quantidade disponivel de um {@link Ingrediente} no estoque de uma {@link Escola}.
 * A combinacao (escola, ingrediente) e unica.
 */
@Entity
@Table(
        name = "estoque",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_estoque_escola_ingrediente",
                columnNames = {"escola_id", "ingrediente_id"}
        )
)
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "escola_id", nullable = false)
    private Escola escola;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    @Column(name = "quantidade_disponivel", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidadeDisponivel;

    protected Estoque() {
        // exigido pelo JPA
    }

    public Estoque(Escola escola, Ingrediente ingrediente, BigDecimal quantidadeDisponivel) {
        this.escola = escola;
        this.ingrediente = ingrediente;
        this.quantidadeDisponivel = quantidadeDisponivel;
    }

    /** Indica se o estoque comporta a quantidade solicitada. */
    public boolean suporta(BigDecimal quantidade) {
        return quantidadeDisponivel.compareTo(quantidade) >= 0;
    }

    /**
     * Desconta a quantidade informada do estoque disponivel.
     *
     * @throws IllegalArgumentException se a quantidade for maior que o disponivel
     */
    public void descontar(BigDecimal quantidade) {
        if (!suporta(quantidade)) {
            throw new IllegalArgumentException(
                    "Quantidade a descontar (" + quantidade + ") maior que o disponivel (" + quantidadeDisponivel + ")");
        }
        this.quantidadeDisponivel = this.quantidadeDisponivel.subtract(quantidade);
    }

    public Long getId() {
        return id;
    }

    public Escola getEscola() {
        return escola;
    }

    public Ingrediente getIngrediente() {
        return ingrediente;
    }

    public BigDecimal getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public void setQuantidadeDisponivel(BigDecimal quantidadeDisponivel) {
        this.quantidadeDisponivel = quantidadeDisponivel;
    }
}
