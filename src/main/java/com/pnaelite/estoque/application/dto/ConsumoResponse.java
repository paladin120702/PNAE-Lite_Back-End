package com.pnaelite.estoque.application.dto;

import java.util.List;

/**
 * Resultado de um consumo registrado com sucesso.
 *
 * @param mensagem         mensagem de sucesso
 * @param cardapio         nome do cardapio consumido
 * @param quantidadeAlunos numero de alunos atendidos
 * @param descontos        descontos aplicados por ingrediente
 */
public record ConsumoResponse(
        String mensagem,
        String cardapio,
        int quantidadeAlunos,
        List<DescontoResponse> descontos
) {
}
