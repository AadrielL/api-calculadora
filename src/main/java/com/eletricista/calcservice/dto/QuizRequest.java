package com.eletricista.calcservice.dto;

import java.util.List;

public record QuizRequest(
        String nomeCliente,
        Double metragemM2,
        Integer qtdPontosEletrica,
        String complexidade,
        Boolean comInfra, // Novo campo crucial
        List<ItemAdicionalDTO> adicionais,
        Double distanciaQuadroPoste,
        Integer visitasEstimadas,
        Double diariaAjudante,
        Integer qtdArCondicionado,
        Integer qtdChuveiro
) {}