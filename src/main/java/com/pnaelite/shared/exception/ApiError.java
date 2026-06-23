package com.pnaelite.shared.exception;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Corpo padrao de resposta de erro da API.
 *
 * @param timestamp momento do erro
 * @param status    codigo HTTP
 * @param error     nome do status HTTP
 * @param message   mensagem legivel para o cliente
 * @param details   informacoes adicionais (opcional), ex.: erros de validacao por campo
 */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, Object> details
) {
    public static ApiError of(int status, String error, String message, Map<String, Object> details) {
        return new ApiError(OffsetDateTime.now(), status, error, message, details);
    }
}
