package com.pnaelite.estoque.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Requisicao de registro de consumo de merenda.
 *
 * @param escolaId          escola que esta consumindo o estoque
 * @param cardapioId        cardapio selecionado
 * @param quantidadeAlunos  numero de alunos atendidos (>= 1)
 */
public record ConsumoRequest(
        @NotNull(message = "escolaId e obrigatorio")
        Long escolaId,

        @NotNull(message = "cardapioId e obrigatorio")
        Long cardapioId,

        @NotNull(message = "quantidadeAlunos e obrigatorio")
        @Min(value = 1, message = "quantidadeAlunos deve ser no minimo 1")
        Integer quantidadeAlunos
) {
}
