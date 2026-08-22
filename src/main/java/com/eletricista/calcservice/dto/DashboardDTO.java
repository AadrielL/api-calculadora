package com.eletricista.calcservice.dto;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private long totalOrcamentos;
    private Map<String, Long> statusCount; // Ex: { "ACEITO": 10, "PENDENTE_ADMIN": 5 }
    private double taxaConversao;
    private long budgetsToday;
    private int dailyLimit;
    private String planType;
}