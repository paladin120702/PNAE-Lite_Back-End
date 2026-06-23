package com.pnaelite.cardapio.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnaelite.cardapio.application.CardapioService;
import com.pnaelite.cardapio.application.dto.CardapioResponse;

@RestController
@RequestMapping("/cardapios")
public class CardapioController {

    private final CardapioService cardapioService;

    public CardapioController(CardapioService cardapioService) {
        this.cardapioService = cardapioService;
    }

    @GetMapping
    public List<CardapioResponse> listar() {
        return cardapioService.listar();
    }
}
