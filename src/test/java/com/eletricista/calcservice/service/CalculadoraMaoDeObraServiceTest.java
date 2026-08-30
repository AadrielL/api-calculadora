package com.eletricista.calcservice.service;

import com.eletricista.calcservice.dto.ItemAdicionalDTO;
import com.eletricista.calcservice.dto.OrcamentoResponse;
import com.eletricista.calcservice.dto.QuizRequest;
import com.eletricista.calcservice.model.Configuracao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraMaoDeObraServiceTest {

    private CalculadoraMaoDeObraService calculadoraService;
    private Configuracao configuracaoPadrao;

    @BeforeEach
    void setUp() {
        calculadoraService = new CalculadoraMaoDeObraService();
        configuracaoPadrao = new Configuracao();
        configuracaoPadrao.setValorM2ComInfra(35.0);
        configuracaoPadrao.setValorM2SemInfra(18.0);
        configuracaoPadrao.setAreaBase(100.0);
        configuracaoPadrao.setPontosBase(30);
        configuracaoPadrao.setValorPontoExtra(55.0);
        configuracaoPadrao.setPrecoMotorPortao(350.0);
        configuracaoPadrao.setPrecoCamera(150.0);
        configuracaoPadrao.setPrecoCercaMetro(25.0);
        configuracaoPadrao.setPrecoInstalacaoAr(150.0);
        configuracaoPadrao.setPrecoInstalacaoChuveiro(80.0);
        configuracaoPadrao.setFatorReforma(1.3);
        configuracaoPadrao.setFatorAltoPadrao(1.6);
    }

    @Test
    @DisplayName("Deve calcular orçamento residencial padrão com infraestrutura corretamente")
    void deveCalcularOrcamentoPadraoComSucesso() {
        QuizRequest quiz = new QuizRequest(
                "João Silva",
                100.0,
                30,
                "RESIDENCIAL",
                true,
                1,
                150.0,
                0, 0, 0, 0, 0.0, 15.0,
                List.of()
        );

        OrcamentoResponse response = calculadoraService.calcularOrcamentoCompleto(quiz, configuracaoPadrao);

        assertNotNull(response);
        assertEquals(3500.0, response.valorTotalMaoDeObra(), 0.01);
        assertEquals(1140.0, response.custoLogistica(), 0.01);
        assertEquals("CALCULADO", response.status());
    }

    @Test
    @DisplayName("Deve cobrar pontos extras quando exceder o limite da área base")
    void deveCalcularComPontosExtras() {
        QuizRequest quiz = new QuizRequest(
                "Maria Souza",
                100.0,
                35, // 5 pontos a mais que os 30 permitidos
                "RESIDENCIAL",
                true,
                1,
                150.0,
                0, 0, 0, 0, 0.0, 15.0,
                List.of()
        );

        OrcamentoResponse response = calculadoraService.calcularOrcamentoCompleto(quiz, configuracaoPadrao);

        // 100 * 35 = 3500 + (5 * 55) = 3500 + 275 = 3775.0
        assertEquals(3775.0, response.valorTotalMaoDeObra(), 0.01);
    }

    @Test
    @DisplayName("Deve aplicar fator multiplicador para projetos de Reforma")
    void deveAplicarFatorReforma() {
        QuizRequest quiz = new QuizRequest(
                "Carlos Reforma",
                100.0,
                30,
                "REFORMA",
                true,
                1,
                150.0,
                0, 0, 0, 0, 0.0, 15.0,
                List.of()
        );

        OrcamentoResponse response = calculadoraService.calcularOrcamentoCompleto(quiz, configuracaoPadrao);

        // 3500.0 * 1.3 = 4550.0
        assertEquals(4550.0, response.valorTotalMaoDeObra(), 0.01);
    }

    @Test
    @DisplayName("Deve somar itens adicionais dinâmicos selecionados")
    void deveCalcularAdicionaisDinamicos() {
        ItemAdicionalDTO item1 = new ItemAdicionalDTO("Ponto 220V", 100.0, 2, true);
        ItemAdicionalDTO itemNaoSelecionado = new ItemAdicionalDTO("Luminária", 80.0, 3, false);

        QuizRequest quiz = new QuizRequest(
                "Ana Paula",
                100.0,
                30,
                "RESIDENCIAL",
                true,
                1,
                150.0,
                0, 0, 0, 0, 0.0, 15.0,
                List.of(item1, itemNaoSelecionado)
        );

        OrcamentoResponse response = calculadoraService.calcularOrcamentoCompleto(quiz, configuracaoPadrao);

        // Base 3500.0 + (2 * 100.0) = 3700.0
        assertEquals(3700.0, response.valorTotalMaoDeObra(), 0.01);
    }
}
