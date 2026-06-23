package com.pnaelite.shared.exception;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Regra critica: estoque insuficiente -> 422 Unprocessable Entity. */
    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ApiError> handleEstoqueInsuficiente(EstoqueInsuficienteException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("ingrediente", ex.getIngrediente());
        details.put("unidadeMedida", ex.getUnidadeMedida());
        details.put("necessario", ex.getNecessario());
        details.put("disponivel", ex.getDisponivel());

        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(), ex.getMessage(), details);
        return ResponseEntity.status(status).body(body);
    }

    /** Entidade nao encontrada -> 404 Not Found. */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(), ex.getMessage(), null);
        return ResponseEntity.status(status).body(body);
    }

    /** Falha de validacao do corpo da requisicao -> 400 Bad Request. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, Object> campos = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            campos.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError body = ApiError.of(
                status.value(), status.getReasonPhrase(),
                "Requisicao invalida.", Map.of("campos", campos));
        return ResponseEntity.status(status).body(body);
    }

    /** Tipo de argumento invalido (ex.: regra de dominio violada) -> 400 Bad Request. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(), ex.getMessage(), null);
        return ResponseEntity.status(status).body(body);
    }
}
