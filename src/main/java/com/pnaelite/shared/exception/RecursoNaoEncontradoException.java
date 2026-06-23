package com.pnaelite.shared.exception;

/**
 * Lancada quando uma entidade referenciada (escola, cardapio, estoque) nao existe.
 * Mapeada para HTTP 404 (Not Found) pelo {@link GlobalExceptionHandler}.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
