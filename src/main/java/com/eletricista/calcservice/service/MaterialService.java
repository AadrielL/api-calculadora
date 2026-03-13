package com.eletricista.calcservice.service;

import com.eletricista.calcservice.dto.QuizRequest;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialService {

    public List<String> gerarListaMateriais(QuizRequest quiz) {
        List<String> lista = new ArrayList<>();
        double m2 = quiz.metragemM2();

        // Evita erro se a metragem vier nula
        double metragemFioBase = (m2 > 0) ? m2 * 1.5 : 0;

        // Cálculo da Entrada baseado na distância
        double distancia = (quiz.distanciaQuadroPoste() != null) ? quiz.distanciaQuadroPoste() : 0;
        String bitolaEntrada = (distancia > 25) ? "16mm²" : "10mm²";

        lista.add("Entrada: " + String.format("%.2f", distancia * 1.2) + "m de fio " + bitolaEntrada);
        lista.add("Iluminação: " + String.format("%.2f", metragemFioBase * 0.4) + "m de fio 1,5mm² (Azul/Verde/Preto)");
        lista.add("Tomadas TUG: " + String.format("%.2f", metragemFioBase * 0.8) + "m de fio 2,5mm² (Azul/Verde/Vermelho)");

        // CORREÇÃO: Usando qtdChuveiro (Integer) em vez de temChuveiro (Boolean)
        if (quiz.qtdChuveiro() != null && quiz.qtdChuveiro() > 0) {
            lista.add("Chuveiros (" + quiz.qtdChuveiro() + " unidades): Recomenda-se 40m de fio 6mm² por unidade");
        }

        // CORREÇÃO: Usando qtdArCondicionado (Integer) em vez de temArCondicionado (Boolean)
        if (quiz.qtdArCondicionado() != null && quiz.qtdArCondicionado() > 0) {
            String bAr = (m2 > 100) ? "4mm²" : "2,5mm²";
            lista.add("Ar Condicionado (" + quiz.qtdArCondicionado() + " unidades): Recomendado Fio " + bAr);
        }

        return lista;
    }

    public String sugerirEquilibrioFases() {
        return "Sugestão Profissional -> Fase R: Tomadas | Fase S: Chuveiro | Fase T: Ar/Motores. Cores: Preto, Vermelho, Branco.";
    }
}