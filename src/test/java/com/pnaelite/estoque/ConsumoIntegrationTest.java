package com.pnaelite.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.pnaelite.cardapio.domain.Cardapio;
import com.pnaelite.cardapio.domain.CardapioRepository;
import com.pnaelite.estoque.application.ConsumoService;
import com.pnaelite.estoque.application.dto.ConsumoRequest;
import com.pnaelite.estoque.domain.Escola;
import com.pnaelite.estoque.domain.EscolaRepository;
import com.pnaelite.estoque.domain.Estoque;
import com.pnaelite.estoque.domain.EstoqueRepository;
import com.pnaelite.estoque.domain.Ingrediente;
import com.pnaelite.estoque.domain.IngredienteRepository;
import com.pnaelite.shared.exception.EstoqueInsuficienteException;

/**
 * Testes de integracao com banco H2 real.
 *
 * <p>O {@link ConsumoService#registrar} e {@code @Transactional}; como os metodos de teste
 * NAO sao transacionais, o servico roda em sua propria transacao que faz commit (sucesso)
 * ou rollback (falha). Apos a chamada, relemos o estoque do banco para comprovar o efeito.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConsumoIntegrationTest {

    @Autowired
    private ConsumoService consumoService;

    @Autowired
    private EscolaRepository escolaRepository;
    @Autowired
    private IngredienteRepository ingredienteRepository;
    @Autowired
    private EstoqueRepository estoqueRepository;
    @Autowired
    private CardapioRepository cardapioRepository;

    private Long escolaId;
    private Long arrozId;
    private Long feijaoId;
    private Long cardapioId;

    @BeforeEach
    void setUp() {
        // Limpeza (ordem respeita as FKs; cardapio remove os itens via cascade)
        cardapioRepository.deleteAll();
        estoqueRepository.deleteAll();
        ingredienteRepository.deleteAll();
        escolaRepository.deleteAll();

        Escola escola = escolaRepository.save(new Escola("Escola Integracao"));
        Ingrediente arroz = ingredienteRepository.save(new Ingrediente("Arroz", "g"));
        Ingrediente feijao = ingredienteRepository.save(new Ingrediente("Feijao", "g"));
        Ingrediente carne = ingredienteRepository.save(new Ingrediente("Carne moida", "g"));

        // Carne e o gargalo: so suporta 50 alunos (2500 / 50)
        estoqueRepository.save(new Estoque(escola, arroz, new BigDecimal("6000")));
        estoqueRepository.save(new Estoque(escola, feijao, new BigDecimal("5000")));
        estoqueRepository.save(new Estoque(escola, carne, new BigDecimal("2500")));

        Cardapio cardapio = new Cardapio("Arroz, feijao e carne", "Almoco");
        cardapio.adicionarItem(arroz, new BigDecimal("80"));  // 80 g/aluno
        cardapio.adicionarItem(feijao, new BigDecimal("60")); // 60 g/aluno
        cardapio.adicionarItem(carne, new BigDecimal("50"));  // 50 g/aluno
        cardapio = cardapioRepository.save(cardapio);

        escolaId = escola.getId();
        arrozId = arroz.getId();
        feijaoId = feijao.getId();
        cardapioId = cardapio.getId();
    }

    @Test
    @DisplayName("Consumo valido persiste os descontos no banco")
    void consumoValido_persisteDescontos() {
        consumoService.registrar(new ConsumoRequest(escolaId, cardapioId, 10)); // 10 alunos

        assertThat(quantidadeEmEstoque(arrozId)).isEqualByComparingTo("5200");  // 6000 - 800
        assertThat(quantidadeEmEstoque(feijaoId)).isEqualByComparingTo("4400"); // 5000 - 600
    }

    @Test
    @DisplayName("Regra Critica: estoque insuficiente lanca 422 e faz ROLLBACK de tudo no banco")
    void estoqueInsuficiente_fazRollbackCompleto() {
        // 60 alunos: arroz (4800<=6000) e feijao (3600<=5000) passam, mas carne (3000>2500) falha.
        assertThatThrownBy(() -> consumoService.registrar(
                new ConsumoRequest(escolaId, cardapioId, 60)))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("Carne moida");

        // Nada pode ter sido descontado: arroz e feijao devem estar intactos no banco.
        assertThat(quantidadeEmEstoque(arrozId)).isEqualByComparingTo("6000");
        assertThat(quantidadeEmEstoque(feijaoId)).isEqualByComparingTo("5000");
    }

    private BigDecimal quantidadeEmEstoque(Long ingredienteId) {
        Estoque estoque = estoqueRepository
                .findByEscolaIdAndIngredienteId(escolaId, ingredienteId)
                .orElseThrow();
        return estoque.getQuantidadeDisponivel();
    }
}
