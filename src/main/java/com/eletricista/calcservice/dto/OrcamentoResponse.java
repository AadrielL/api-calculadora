package com.eletricista.calcservice.dto;

import java.util.List;
import java.util.UUID; // Importação essencial

public record OrcamentoResponse(
        UUID id, // Alterado de Long para UUID
        Double valorTotalMaoDeObra,
        Double custoLogistica,
        List<String> listaMateriais,
        String equilibrioFases,
        String status
) {}