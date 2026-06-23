package com.pnaelite.estoque.web;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnaelite.estoque.application.ConsumoService;
import com.pnaelite.estoque.application.dto.ConsumoRequest;
import com.pnaelite.estoque.application.dto.ConsumoResponse;

@RestController
@RequestMapping("/consumo")
public class ConsumoController {

    private final ConsumoService consumoService;

    public ConsumoController(ConsumoService consumoService) {
        this.consumoService = consumoService;
    }

    @PostMapping
    public ConsumoResponse registrar(@Valid @RequestBody ConsumoRequest request) {
        return consumoService.registrar(request);
    }
}
