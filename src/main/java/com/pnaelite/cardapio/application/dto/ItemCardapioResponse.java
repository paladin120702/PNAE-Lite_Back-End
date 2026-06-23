package com.pnaelite.cardapio.application.dto;

import java.math.BigDecimal;

import com.pnaelite.cardapio.domain.ItemCardapio;

public record ItemCardapioResponse(
        Long ingredienteId,
        String ingrediente,
        String unidadeMedida,
        BigDecimal quantidadePorAluno
) {
    public static ItemCardapioResponse from(ItemCardapio item) {
        return new ItemCardapioResponse(
                item.getIngrediente().getId(),
                item.getIngrediente().getNome(),
                item.getIngrediente().getUnidadeMedida(),
                item.getQuantidadePorAluno()
        );
    }
}
