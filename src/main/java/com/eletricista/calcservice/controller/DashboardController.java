package com.eletricista.calcservice.controller;

import com.eletricista.calcservice.dto.DashboardDTO;
import com.eletricista.calcservice.model.StatusOrcamento;
import com.eletricista.calcservice.repository.OrcamentoRepository;
import com.eletricista.calcservice.config.TenantContext;
import org.springframework.web.bind.annotation.CrossOrigin; // Importante!
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
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

        // Log para você ver no IntelliJ se o Tenant está chegando
        System.out.println("Buscando stats para o Tenant: " + tenantId);

        List<Object[]> results = repository.countStatusByTenant(tenantId);
        Map<String, Long> statsMap = new HashMap<>();

        for (Object[] result : results) {
            statsMap.put(result[0].toString(), (Long) result[1]);
        }

        long total = repository.countByTenantId(tenantId);
        long aceitos = repository.countByStatusAndTenantId(StatusOrcamento.ACEITO, tenantId);
        double conversao = (total > 0) ? ((double) aceitos / total) * 100 : 0;

        return new DashboardDTO(total, statsMap, conversao);
    }
}