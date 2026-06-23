package com.pnaelite.estoque.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pnaelite.cardapio.domain.Cardapio;
import com.pnaelite.cardapio.domain.CardapioRepository;
import com.pnaelite.cardapio.domain.ItemCardapio;
import com.pnaelite.estoque.application.dto.ConsumoRequest;
import com.pnaelite.estoque.application.dto.ConsumoResponse;
import com.pnaelite.estoque.application.dto.DescontoResponse;
import com.pnaelite.estoque.domain.Estoque;
import com.pnaelite.estoque.domain.EstoqueRepository;
import com.pnaelite.estoque.domain.Ingrediente;
import com.pnaelite.shared.exception.EstoqueInsuficienteException;
import com.pnaelite.shared.exception.RecursoNaoEncontradoException;

@Service
public class ConsumoService {

    private final CardapioRepository cardapioRepository;
    private final EstoqueRepository estoqueRepository;

    public ConsumoService(CardapioRepository cardapioRepository, EstoqueRepository estoqueRepository) {
        this.cardapioRepository = cardapioRepository;
        this.estoqueRepository = estoqueRepository;
    }

    /**
     * Registra o consumo de um cardapio para uma quantidade de alunos, descontando os
     * ingredientes do estoque da escola.
     *
     * <p>Todo o metodo roda dentro de uma unica transacao. Se qualquer ingrediente nao
     * tiver estoque suficiente, lanca {@link EstoqueInsuficienteException} (RuntimeException),
     * o que faz o Spring/JPA reverter (<i>rollback</i>) todos os descontos ja aplicados nesta
     * transacao — garantindo a Regra Critica: nada e persistido em caso de falta de estoque.
     *
     * @throws RecursoNaoEncontradoException  se o cardapio ou o estoque nao existirem (404)
     * @throws EstoqueInsuficienteException   se faltar estoque para algum ingrediente (422)
     */
    @Transactional
    public ConsumoResponse registrar(ConsumoRequest request) {
        Cardapio cardapio = cardapioRepository.findById(request.cardapioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cardapio nao encontrado: id=" + request.cardapioId()));

        int alunos = request.quantidadeAlunos();
        List<DescontoResponse> descontos = new ArrayList<>();

        for (ItemCardapio item : cardapio.getItens()) {
            Ingrediente ingrediente = item.getIngrediente();
            BigDecimal necessario = item.calcularNecessario(alunos);

            Estoque estoque = estoqueRepository
                    .findByEscolaIdAndIngredienteId(request.escolaId(), ingrediente.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Sem estoque cadastrado para o ingrediente '" + ingrediente.getNome()
                                    + "' na escola id=" + request.escolaId()));

            if (!estoque.suporta(necessario)) {
                // Dispara o rollback de tudo que foi descontado ate aqui nesta transacao.
                throw new EstoqueInsuficienteException(
                        ingrediente.getNome(), necessario,
                        estoque.getQuantidadeDisponivel(), ingrediente.getUnidadeMedida());
            }

            estoque.descontar(necessario);
            descontos.add(new DescontoResponse(
                    ingrediente.getNome(), ingrediente.getUnidadeMedida(),
                    necessario, estoque.getQuantidadeDisponivel()));
        }

        return new ConsumoResponse(
                "Consumo registrado com sucesso.", cardapio.getNome(), alunos, descontos);
    }
}
