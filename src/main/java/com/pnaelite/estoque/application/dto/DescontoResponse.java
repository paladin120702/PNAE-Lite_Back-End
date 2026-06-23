package com.pnaelite.estoque.application.dto;

import java.math.BigDecimal;

/**
 * Detalhe do desconto aplicado a um ingrediente apos o consumo.
 *
 * @param ingrediente         nome do ingrediente
 * @param unidadeMedida       unidade de medida
 * @param quantidadeDescontada quantidade descontada do estoque
 * @param quantidadeRestante  quantidade que sobrou no estoque
 */
public record DescontoResponse(
        String ingrediente,
        String unidadeMedida,
        BigDecimal quantidadeDescontada,
        BigDecimal quantidadeRestante
) {
}
