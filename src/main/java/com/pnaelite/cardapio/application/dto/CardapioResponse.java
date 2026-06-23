package com.pnaelite.cardapio.application.dto;

import java.util.List;

import com.pnaelite.cardapio.domain.Cardapio;

public record CardapioResponse(
        Long id,
        String nome,
        String descricao,
        List<ItemCardapioResponse> itens
) {
    public static CardapioResponse from(Cardapio cardapio) {
        List<ItemCardapioResponse> itens = cardapio.getItens().stream()
                .map(ItemCardapioResponse::from)
                .toList();
        return new CardapioResponse(cardapio.getId(), cardapio.getNome(), cardapio.getDescricao(), itens);
    }
}
