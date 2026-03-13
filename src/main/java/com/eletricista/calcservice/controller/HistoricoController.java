package com.eletricista.calcservice.controller;

import com.eletricista.calcservice.model.Orcamento;
import com.eletricista.calcservice.repository.OrcamentoRepository;
import com.eletricista.calcservice.config.TenantContext;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
}