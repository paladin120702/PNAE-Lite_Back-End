package com.pnaelite.shared.exception;

import java.math.BigDecimal;

/**
 * Lancada quando o consumo solicitado excede o estoque disponivel de algum ingrediente.
 * Como e uma RuntimeException, dispara o rollback da transacao JPA.
 * Mapeada para HTTP 422 (Unprocessable Entity) pelo {@link GlobalExceptionHandler}.
 */
public class EstoqueInsuficienteException extends RuntimeException {

    private final String ingrediente;
    private final BigDecimal necessario;
    private final BigDecimal disponivel;
    private final String unidadeMedida;

    public EstoqueInsuficienteException(String ingrediente, BigDecimal necessario,
                                        BigDecimal disponivel, String unidadeMedida) {
        super(String.format(
                "Estoque insuficiente de '%s': necessario %s %s, disponivel %s %s.",
                ingrediente, necessario, unidadeMedida, disponivel, unidadeMedida));
        this.ingrediente = ingrediente;
        this.necessario = necessario;
        this.disponivel = disponivel;
        this.unidadeMedida = unidadeMedida;
    }

    public String getIngrediente() {
        return ingrediente;
    }

    public BigDecimal getNecessario() {
        return necessario;
    }

    public BigDecimal getDisponivel() {
        return disponivel;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }
}
