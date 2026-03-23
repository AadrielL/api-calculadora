package com.eletricista.calcservice.dto;

import java.util.List;

public record QuizRequest(
        String nomeCliente,
        Double metragemM2,
        Integer qtdPontosEletrica,
        String complexidade,
        Boolean comInfra,
        Integer visitasEstimadas,
        Double diariaAjudante,
        Integer qtdArCondicionado,
        Integer qtdChuveiro,
        Integer qtdCameras,
        Integer qtdMotoresPortao,
        Double metrosCerca,
        Double distanciaQuadroPoste, // O campo que estava faltando!
        List<ItemAdicionalDTO> adicionais
) {}