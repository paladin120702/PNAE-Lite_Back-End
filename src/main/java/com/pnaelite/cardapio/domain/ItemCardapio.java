package com.pnaelite.cardapio.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

import com.pnaelite.estoque.domain.Ingrediente;

/**
 * Entidade associativa entre {@link Cardapio} e {@link Ingrediente}.
 * Guarda a quantidade do ingrediente consumida por aluno naquele cardapio.
 */
@Entity
@Table(name = "item_cardapio")
public class ItemCardapio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cardapio_id", nullable = false)
    private Cardapio cardapio;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    @Column(name = "quantidade_por_aluno", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidadePorAluno;

    protected ItemCardapio() {
        // exigido pelo JPA
    }

    public ItemCardapio(Cardapio cardapio, Ingrediente ingrediente, BigDecimal quantidadePorAluno) {
        this.cardapio = cardapio;
        this.ingrediente = ingrediente;
        this.quantidadePorAluno = quantidadePorAluno;
    }

    /** Quantidade total necessaria do ingrediente para a quantidade de alunos informada. */
    public BigDecimal calcularNecessario(int quantidadeAlunos) {
        return quantidadePorAluno.multiply(BigDecimal.valueOf(quantidadeAlunos));
    }

    public Long getId() {
        return id;
    }

    public Cardapio getCardapio() {
        return cardapio;
    }

    public Ingrediente getIngrediente() {
        return ingrediente;
    }

    public BigDecimal getQuantidadePorAluno() {
        return quantidadePorAluno;
    }
}
