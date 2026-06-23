package com.pnaelite.cardapio.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.pnaelite.estoque.domain.Ingrediente;

@Entity
@Table(name = "cardapio")
public class Cardapio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column
    private String descricao;

    @OneToMany(
            mappedBy = "cardapio",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ItemCardapio> itens = new ArrayList<>();

    protected Cardapio() {
        // exigido pelo JPA
    }

    public Cardapio(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    /** Adiciona um ingrediente ao cardapio com a quantidade consumida por aluno. */
    public ItemCardapio adicionarItem(Ingrediente ingrediente, BigDecimal quantidadePorAluno) {
        ItemCardapio item = new ItemCardapio(this, ingrediente, quantidadePorAluno);
        this.itens.add(item);
        return item;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public List<ItemCardapio> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
