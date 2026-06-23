package com.pnaelite.cardapio.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pnaelite.cardapio.application.dto.CardapioResponse;
import com.pnaelite.cardapio.domain.CardapioRepository;

@Service
public class CardapioService {

    private final CardapioRepository cardapioRepository;

    public CardapioService(CardapioRepository cardapioRepository) {
        this.cardapioRepository = cardapioRepository;
    }

    /** Lista todos os cardapios com seus itens (ingrediente + quantidade por aluno). */
    @Transactional(readOnly = true)
    public List<CardapioResponse> listar() {
        return cardapioRepository.findAll().stream()
                .map(CardapioResponse::from)
                .toList();
    }
}
