package com.eletricista.calcservice.dto;

import java.util.List;

public record QuizRequest(
        String nomeCliente,
        Double metragemM2,
        Integer qtdPontosEletrica,
        String complexidade, // "RESIDENCIAL", "COMERCIAL", "REFORMA", "ALTO_PADRAO"
        Boolean comInfra,
        List<ItemAdicionalDTO> adicionais,
        Double distanciaQuadroPoste,
        Integer visitasEstimadas,
        Double diariaAjudante,
        // Campos de contagem (se não tiver, o front envia 0)
        Integer qtdArCondicionado,
        Integer qtdChuveiro,
        Integer qtdCameras,
        Integer qtdMotoresPortao,
        Double metrosCerca // em metros lineares
) {}