package com.eletricista.calcservice.service;

import com.eletricista.calcservice.dto.*;
import com.eletricista.calcservice.model.*;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CalculadoraMaoDeObraService {

    public OrcamentoResponse calcularOrcamentoCompleto(QuizRequest quiz, Configuracao conf) {
        if (conf == null) conf = new Configuracao();

        double valorAcumulado = 0.0;

        // 1. BASE E PONTOS EXTRAS
        valorAcumulado += calcularBaseEPontos(quiz, conf);

        // 2. ITENS FIXOS (BANCO)
        valorAcumulado += calcularItensFixos(quiz, conf);

        // 3. ITENS DINÂMICOS (FRONT)
        valorAcumulado += calcularItensFront(quiz);

        // 4. COMPLEXIDADE
        double totalMaoDeObra = aplicarFatorComplexidade(valorAcumulado, quiz, conf);

        // 5. LOGÍSTICA
        double custoLogistica = calcularLogistica(quiz);

        return new OrcamentoResponse(null, totalMaoDeObra, custoLogistica, null, "Cálculo Realizado", "CALCULADO");
    }

    private double calcularBaseEPontos(QuizRequest quiz, Configuracao conf) {
        double metragem = Optional.ofNullable(quiz.metragemM2()).orElse(0.0);
        double valorM2 = Boolean.TRUE.equals(quiz.comInfra()) ?
                Optional.ofNullable(conf.getValorM2ComInfra()).orElse(0.0) :
                Optional.ofNullable(conf.getValorM2SemInfra()).orElse(0.0);

        double base = metragem * valorM2;

        double areaBase = Optional.ofNullable(conf.getAreaBase()).orElse(100.0);
        int pontosBase = Optional.ofNullable(conf.getPontosBase()).orElse(30);
        int permitidos = pontosBase + ((int) (Math.floor(Math.max(0, metragem - areaBase) / 5.0) * 2));

        int solicitados = Optional.ofNullable(quiz.qtdPontosEletrica()).orElse(0);
        if (solicitados > permitidos) {
            base += (solicitados - permitidos) * Optional.ofNullable(conf.getValorPontoExtra()).orElse(0.0);
        }
        return base;
    }

    private double calcularItensFixos(QuizRequest quiz, Configuracao conf) {
        double soma = 0;
        soma += multiplicar(quiz.qtdMotoresPortao(), conf.getPrecoMotorPortao());
        soma += multiplicar(quiz.qtdCameras(), conf.getPrecoCamera());
        soma += multiplicar(quiz.metrosCerca(), conf.getPrecoCercaMetro());
        soma += multiplicar(quiz.qtdArCondicionado(), conf.getPrecoInstalacaoAr());
        soma += multiplicar(quiz.qtdChuveiro(), conf.getPrecoInstalacaoChuveiro());
        return soma;
    }

    private double calcularItensFront(QuizRequest quiz) {
        if (quiz.adicionais() == null) return 0.0;
        return quiz.adicionais().stream()
                .filter(item -> Boolean.TRUE.equals(item.selecionado()) && item.precoUnitario() != null)
                .mapToDouble(item -> item.quantidade() * item.precoUnitario())
                .sum();
    }

    private double aplicarFatorComplexidade(double valor, QuizRequest quiz, Configuracao conf) {
        double fator = 1.0;
        if ("REFORMA".equalsIgnoreCase(quiz.complexidade())) {
            fator = Optional.ofNullable(conf.getFatorReforma()).orElse(1.3);
        } else if (Optional.ofNullable(quiz.metragemM2()).orElse(0.0) > 150
                || "ALTO_PADRAO".equalsIgnoreCase(quiz.complexidade())) {
            fator = Optional.ofNullable(conf.getFatorAltoPadrao()).orElse(1.6);
        }
        return valor * fator;
    }

    private double calcularLogistica(QuizRequest quiz) {
        double metragem = Optional.ofNullable(quiz.metragemM2()).orElse(0.0);
        int dias = (int) Math.ceil(metragem / 30.0) + 2;
        double diaria = (quiz.diariaAjudante() != null && quiz.diariaAjudante() > 0) ? quiz.diariaAjudante() : 150.0;
        return dias * (diaria + 40.0);
    }

    private double multiplicar(Object qtd, Double preco) {
        if (qtd == null || preco == null) return 0.0;
        try {
            double quantidade = Double.parseDouble(qtd.toString());
            return (quantidade > 0) ? quantidade * preco : 0.0;
        } catch (Exception e) { return 0.0; }
    }
}