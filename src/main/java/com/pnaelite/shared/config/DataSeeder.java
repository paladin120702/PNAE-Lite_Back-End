package com.pnaelite.shared.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pnaelite.cardapio.domain.Cardapio;
import com.pnaelite.cardapio.domain.CardapioRepository;
import com.pnaelite.estoque.domain.Escola;
import com.pnaelite.estoque.domain.EscolaRepository;
import com.pnaelite.estoque.domain.Estoque;
import com.pnaelite.estoque.domain.EstoqueRepository;
import com.pnaelite.estoque.domain.Ingrediente;
import com.pnaelite.estoque.domain.IngredienteRepository;

/**
 * Popula o banco H2 com dados de exemplo na inicializacao.
 * Desativado no profile de testes ("test"), onde cada teste prepara seus proprios dados.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final EscolaRepository escolaRepository;
    private final IngredienteRepository ingredienteRepository;
    private final EstoqueRepository estoqueRepository;
    private final CardapioRepository cardapioRepository;

    public DataSeeder(EscolaRepository escolaRepository,
                      IngredienteRepository ingredienteRepository,
                      EstoqueRepository estoqueRepository,
                      CardapioRepository cardapioRepository) {
        this.escolaRepository = escolaRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.estoqueRepository = estoqueRepository;
        this.cardapioRepository = cardapioRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (cardapioRepository.count() > 0) {
            return; // ja populado
        }

        // ----- Escola -----
        Escola escola = escolaRepository.save(new Escola("EMEF Monteiro Lobato"));

        // ----- Ingredientes -----
        Ingrediente arroz   = ingredienteRepository.save(new Ingrediente("Arroz", "g"));
        Ingrediente feijao  = ingredienteRepository.save(new Ingrediente("Feijao", "g"));
        Ingrediente carne   = ingredienteRepository.save(new Ingrediente("Carne moida", "g"));
        Ingrediente macarrao = ingredienteRepository.save(new Ingrediente("Macarrao", "g"));
        Ingrediente molho   = ingredienteRepository.save(new Ingrediente("Molho de tomate", "ml"));
        Ingrediente leite   = ingredienteRepository.save(new Ingrediente("Leite", "ml"));
        Ingrediente achocolatado = ingredienteRepository.save(new Ingrediente("Achocolatado", "g"));

        // ----- Estoque da escola (quantidades propositalmente limitadas) -----
        estoqueRepository.save(new Estoque(escola, arroz, new BigDecimal("10000")));   // 10 kg
        estoqueRepository.save(new Estoque(escola, feijao, new BigDecimal("8000")));    // 8 kg
        estoqueRepository.save(new Estoque(escola, carne, new BigDecimal("5000")));     // 5 kg
        estoqueRepository.save(new Estoque(escola, macarrao, new BigDecimal("6000")));  // 6 kg
        estoqueRepository.save(new Estoque(escola, molho, new BigDecimal("4000")));     // 4 L
        estoqueRepository.save(new Estoque(escola, leite, new BigDecimal("20000")));    // 20 L
        estoqueRepository.save(new Estoque(escola, achocolatado, new BigDecimal("3000"))); // 3 kg

        // ----- Cardapios -----
        Cardapio prato1 = new Cardapio("Arroz, feijao e carne", "Almoco completo");
        prato1.adicionarItem(arroz, new BigDecimal("80"));   // 80 g/aluno
        prato1.adicionarItem(feijao, new BigDecimal("60"));  // 60 g/aluno
        prato1.adicionarItem(carne, new BigDecimal("50"));   // 50 g/aluno
        cardapioRepository.save(prato1);

        Cardapio prato2 = new Cardapio("Macarrao com molho", "Almoco vegetariano");
        prato2.adicionarItem(macarrao, new BigDecimal("90")); // 90 g/aluno
        prato2.adicionarItem(molho, new BigDecimal("40"));    // 40 ml/aluno
        cardapioRepository.save(prato2);

        Cardapio lanche = new Cardapio("Leite com achocolatado", "Lanche da tarde");
        lanche.adicionarItem(leite, new BigDecimal("200"));        // 200 ml/aluno
        lanche.adicionarItem(achocolatado, new BigDecimal("20"));  // 20 g/aluno
        cardapioRepository.save(lanche);
    }
}
