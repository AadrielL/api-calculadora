package com.eletricista.calcservice.controller;

import com.eletricista.calcservice.dto.DashboardDTO;
import com.eletricista.calcservice.model.StatusOrcamento;
import com.eletricista.calcservice.repository.OrcamentoRepository;
import com.eletricista.calcservice.infra.security.tenant.TenantContext;
import org.springframework.web.bind.annotation.CrossOrigin; // Importante!
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController // <--- ESTA LINHA É OBRIGATÓRIA
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*") // Libera o Angular
public class DashboardController {

    private final OrcamentoRepository repository;

    public DashboardController(OrcamentoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/stats")
    public DashboardDTO getStats() {
        String tenantId = TenantContext.getCurrentTenant();
        String planType = TenantContext.getCurrentPlanType();
        if (planType == null || planType.isEmpty()) {
            planType = "FREE";
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        long budgetsToday = repository.countByTenantIdAndDataCriacao(tenantId, today);

        int dailyLimit = 3;
        if ("WEEKLY".equalsIgnoreCase(planType)) {
            dailyLimit = 50;
        } else if ("MONTHLY".equalsIgnoreCase(planType) || "LIFETIME".equalsIgnoreCase(planType)) {
            dailyLimit = 100;
        }

        System.out.println("Buscando stats para o Tenant: " + tenantId + " | Plano: " + planType + " | Hoje: " + budgetsToday + "/" + dailyLimit);

        List<Object[]> results = repository.countStatusByTenant(tenantId);
        Map<String, Long> statsMap = new HashMap<>();

        for (Object[] result : results) {
            if (result[0] != null) {
                statsMap.put(result[0].toString(), (Long) result[1]);
            }
        }

        long total = repository.countByTenantIdAndExcluidoFalse(tenantId);
        long aceitos = repository.countByStatusAndTenantIdAndExcluidoFalse(StatusOrcamento.ACEITO, tenantId);
        double conversao = (total > 0) ? ((double) aceitos / total) * 100 : 0;

        return new DashboardDTO(total, statsMap, conversao, budgetsToday, dailyLimit, planType);
    }
}