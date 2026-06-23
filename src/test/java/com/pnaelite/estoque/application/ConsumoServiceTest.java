package com.pnaelite.estoque.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.pnaelite.cardapio.domain.Cardapio;
import com.pnaelite.cardapio.domain.CardapioRepository;
import com.pnaelite.estoque.application.dto.ConsumoRequest;
import com.pnaelite.estoque.application.dto.ConsumoResponse;
import com.pnaelite.estoque.domain.Escola;
import com.pnaelite.estoque.domain.Estoque;
import com.pnaelite.estoque.domain.EstoqueRepository;
import com.pnaelite.estoque.domain.Ingrediente;
import com.pnaelite.shared.exception.EstoqueInsuficienteException;
import com.pnaelite.shared.exception.RecursoNaoEncontradoException;

/**
 * Testes unitarios do {@link ConsumoService} com Mockito (repositorios mockados).
 * Aqui validamos a regra de negocio isolada — o rollback no banco e coberto no
 * teste de integracao {@link com.pnaelite.estoque.ConsumoIntegrationTest}.
 */
@ExtendWith(MockitoExtension.class)
class ConsumoServiceTest {

    private static final long ESCOLA_ID = 1L;
    private static final long CARDAPIO_ID = 10L;

    @Mock
    private CardapioRepository cardapioRepository;

    @Mock
    private EstoqueRepository estoqueRepository;

    @InjectMocks
    private ConsumoService consumoService;

    private Ingrediente arroz;
    private Ingrediente feijao;
    private Cardapio cardapio;

    @BeforeEach
    void setUp() {
        arroz = ingredienteComId(1L, "Arroz", "g");
        feijao = ingredienteComId(2L, "Feijao", "g");

        cardapio = new Cardapio("Arroz e feijao", "Almoco");
        cardapio.adicionarItem(arroz, new BigDecimal("80")); // 80 g/aluno
        cardapio.adicionarItem(feijao, new BigDecimal("60")); // 60 g/aluno
    }

    @Test
    @DisplayName("Desconta o estoque corretamente quando ha quantidade suficiente")
    void registrar_comEstoqueSuficiente_descontaCadaIngrediente() {
        Estoque estoqueArroz = new Estoque(escola(), arroz, new BigDecimal("1000"));
        Estoque estoqueFeijao = new Estoque(escola(), feijao, new BigDecimal("1000"));

        when(cardapioRepository.findById(CARDAPIO_ID)).thenReturn(Optional.of(cardapio));
        when(estoqueRepository.findByEscolaIdAndIngredienteId(ESCOLA_ID, 1L))
                .thenReturn(Optional.of(estoqueArroz));
        when(estoqueRepository.findByEscolaIdAndIngredienteId(ESCOLA_ID, 2L))
                .thenReturn(Optional.of(estoqueFeijao));

        ConsumoResponse response = consumoService.registrar(
                new ConsumoRequest(ESCOLA_ID, CARDAPIO_ID, 5)); // 5 alunos

        // 5 alunos: arroz 400 -> resta 600; feijao 300 -> resta 700
        assertThat(estoqueArroz.getQuantidadeDisponivel()).isEqualByComparingTo("600");
        assertThat(estoqueFeijao.getQuantidadeDisponivel()).isEqualByComparingTo("700");
        assertThat(response.descontos()).hasSize(2);
        assertThat(response.quantidadeAlunos()).isEqualTo(5);
    }

    @Test
    @DisplayName("Lanca EstoqueInsuficienteException (422) quando falta estoque de algum ingrediente")
    void registrar_comEstoqueInsuficiente_lancaExcecao() {
        Estoque estoqueArroz = new Estoque(escola(), arroz, new BigDecimal("1000"));
        Estoque estoqueFeijao = new Estoque(escola(), feijao, new BigDecimal("500"));

        when(cardapioRepository.findById(CARDAPIO_ID)).thenReturn(Optional.of(cardapio));
        when(estoqueRepository.findByEscolaIdAndIngredienteId(ESCOLA_ID, 1L))
                .thenReturn(Optional.of(estoqueArroz));
        when(estoqueRepository.findByEscolaIdAndIngredienteId(ESCOLA_ID, 2L))
                .thenReturn(Optional.of(estoqueFeijao));

        // 10 alunos: feijao precisa de 600 mas so ha 500 -> falha
        assertThatThrownBy(() -> consumoService.registrar(
                new ConsumoRequest(ESCOLA_ID, CARDAPIO_ID, 10)))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("Feijao");
    }

    @Test
    @DisplayName("Lanca RecursoNaoEncontradoException quando o cardapio nao existe")
    void registrar_cardapioInexistente_lancaNaoEncontrado() {
        when(cardapioRepository.findById(CARDAPIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumoService.registrar(
                new ConsumoRequest(ESCOLA_ID, CARDAPIO_ID, 5)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Cardapio nao encontrado");
    }

    @Test
    @DisplayName("Lanca RecursoNaoEncontradoException quando nao ha estoque cadastrado do ingrediente")
    void registrar_semEstoqueCadastrado_lancaNaoEncontrado() {
        when(cardapioRepository.findById(CARDAPIO_ID)).thenReturn(Optional.of(cardapio));
        when(estoqueRepository.findByEscolaIdAndIngredienteId(ESCOLA_ID, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumoService.registrar(
                new ConsumoRequest(ESCOLA_ID, CARDAPIO_ID, 5)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Sem estoque cadastrado");
    }

    private static Escola escola() {
        Escola escola = new Escola("Escola Teste");
        ReflectionTestUtils.setField(escola, "id", ESCOLA_ID);
        return escola;
    }

    private static Ingrediente ingredienteComId(Long id, String nome, String unidade) {
        Ingrediente ingrediente = new Ingrediente(nome, unidade);
        ReflectionTestUtils.setField(ingrediente, "id", id);
        return ingrediente;
    }
}
