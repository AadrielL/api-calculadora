package com.eletricista.calcservice.controller;

import com.eletricista.calcservice.model.Orcamento;
import com.eletricista.calcservice.repository.OrcamentoRepository;
import com.eletricista.calcservice.config.TenantContext;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/historico")
@CrossOrigin(origins = "http://localhost:4200")
public class HistoricoController {

    private final OrcamentoRepository repository;

    public HistoricoController(OrcamentoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Orcamento> listarHistorico() {
        String tenantId = TenantContext.getCurrentTenant();
        // Assume-se que o repository tem o método findByTenantIdOrderByDataCriacaoDesc
        return repository.findByTenantIdOrderByDataCriacaoDesc(tenantId);
    }
    @PatchMapping("/{id}/status")
    public void alterarStatus(@PathVariable String id, @RequestParam String novoStatus) {
        // Troque UUID.randomUUID() por UUID.fromString(id)
        Orcamento orc = repository.findById(UUID.fromString(id)).orElseThrow();
        orc.setStatus(com.eletricista.calcservice.model.StatusOrcamento.valueOf(novoStatus.toUpperCase()));
        repository.save(orc);
    }
}