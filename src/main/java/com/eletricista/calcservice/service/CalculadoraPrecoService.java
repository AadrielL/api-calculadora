package com.eletricista.calcservice.service;

import com.eletricista.calcservice.dto.*;
import com.eletricista.calcservice.model.Configuracao;
import org.springframework.stereotype.Service;

@Service
public class CalculadoraPrecoService {

    public Double calcularMaoDeObra(QuizRequest quiz, Configuracao conf) {
        // Esta função agora chama a lógica completa abaixo para não ter código duplicado
        OrcamentoResponse res = calcularOrcamentoCompleto(quiz, conf);
        return res.valorTotalMaoDeObra() + res.custoLogistica();
    }

    public OrcamentoResponse calcularOrcamentoCompleto(QuizRequest quiz, Configuracao conf) {
        if (conf == null) return new OrcamentoResponse(null, 0.0, 0.0, null, null, "ERRO");

        double metragem = (quiz.metragemM2() != null) ? quiz.metragemM2() : 0.0;

        // --- 1. IDENTIFICAÇÃO AUTOMÁTICA DE COMPLEXIDADE ---
        String complexidade;
        int margemDias;

        if (metragem > 150) {
            complexidade = "ALTO_PADRAO";
            margemDias = 10;
        } else if (metragem > 70) {
            complexidade = "MEDIO";
            margemDias = 5;
        } else {
            complexidade = "SIMPLES";
            margemDias = 2;
        }

        // --- 2. CÁLCULO DE TEMPO E LOGÍSTICA (VALORES QUE VOCÊ PEDIU) ---
        int diasBase = (int) Math.ceil(metragem / 30.0);
        int diasTrabalho = diasBase + margemDias;

        // Diária 150 e Gasolina 40
        double vDiaria = (quiz.diariaAjudante() != null && quiz.diariaAjudante() > 0) ? quiz.diariaAjudante() : 150.0;
        double vGasolinaDiaria = 40.0;
        double custoLogisticaTotal = diasTrabalho * (vDiaria + vGasolinaDiaria);

        // --- 3. VALOR TÉCNICO BASE (M2 + PONTOS) - REGRAS ORIGINAIS RESTAURADAS ---
        double vM2Com = (conf.getValorM2ComInfra() != null) ? conf.getValorM2ComInfra() : 0.0;
        double vM2Sem = (conf.getValorM2SemInfra() != null) ? conf.getValorM2SemInfra() : 0.0;
        double valorM2Efetivo = (quiz.comInfra() != null && quiz.comInfra()) ? vM2Com : vM2Sem;

        double valorTecnico = metragem * valorM2Efetivo;

        // Regra de Pontos Bônus (Suas 83 linhas originais voltaram aqui)
        double areaBase = (conf.getAreaBase() != null) ? conf.getAreaBase() : 0.0;
        double stepMetragem = (conf.getStepMetragem() != null && conf.getStepMetragem() > 0) ? conf.getStepMetragem() : 1.0;
        int ganhoPonto = (conf.getGanhoPontosPorStep() != null) ? conf.getGanhoPontosPorStep() : 0;
        int pontosBase = (conf.getPontosBase() != null) ? conf.getPontosBase() : 0;

        double areaExcedente = Math.max(0, metragem - areaBase);
        int limitePontos = pontosBase + (int) (Math.floor(areaExcedente / stepMetragem) * ganhoPonto);

        Integer qtdPontos = (quiz.qtdPontosEletrica() != null) ? quiz.qtdPontosEletrica() : 0;
        if (qtdPontos > limitePontos) {
            valorTecnico += (qtdPontos - limitePontos) * ((conf.getValorPontoExtra() != null) ? conf.getValorPontoExtra() : 0.0);
        }

        // --- 4. INSTALAÇÕES E ADICIONAIS ---
        if (quiz.qtdChuveiro() != null) valorTecnico += quiz.qtdChuveiro() * (conf.getPrecoInstalacaoChuveiro() != null ? conf.getPrecoInstalacaoChuveiro() : 0.0);
        if (quiz.qtdArCondicionado() != null) valorTecnico += quiz.qtdArCondicionado() * (conf.getPrecoInstalacaoAr() != null ? conf.getPrecoInstalacaoAr() : 0.0);

        if (quiz.adicionais() != null) {
            for (ItemAdicionalDTO item : quiz.adicionais()) {
                if (item.tipo() == null) continue;
                valorTecnico += switch (item.tipo().toUpperCase()) {
                    case "CAMERA" -> item.quantidade() * (conf.getPrecoCamera() != null ? conf.getPrecoCamera() : 0.0);
                    case "MOTOR" -> item.quantidade() * (conf.getPrecoMotorPortao() != null ? conf.getPrecoMotorPortao() : 0.0);
                    case "CERCA" -> (item.metrosLineares() != null ? item.metrosLineares() : 0.0) * (conf.getPrecoCercaMetro() != null ? conf.getPrecoCercaMetro() : 0.0);
                    default -> 0.0;
                };
            }
        }

        // --- 5. APLICAÇÃO DO FATOR DE COMPLEXIDADE ---
        double fator = switch (complexidade) {
            case "ALTO_PADRAO" -> (conf.getFatorAltoPadrao() != null ? conf.getFatorAltoPadrao() : 1.5);
            case "MEDIO" -> 1.2;
            default -> 1.0;
        };

        // RETORNO FINAL
        return new OrcamentoResponse(
                null,
                valorTecnico * fator,
                custoLogisticaTotal,
                null,
                "Sucesso",
                "CALCULADO"
        );
    }
}