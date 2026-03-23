package com.eletricista.calcservice.controller;

import com.eletricista.calcservice.model.Orcamento;
import com.eletricista.calcservice.model.StatusOrcamento;
import com.eletricista.calcservice.repository.OrcamentoRepository;
import com.eletricista.calcservice.infra.security.tenant.TenantContext;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
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

    // Listar apenas o que NÃO está na lixeira
    @GetMapping
    public List<Orcamento> listarHistorico() {
        String tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndExcluidoFalseOrderByDataCriacaoDesc(tenantId);
    }

    // Listar apenas o que ESTÁ na lixeira
    @GetMapping("/lixeira")
    public List<Orcamento> listarLixeira() {
        String tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndExcluidoTrueOrderByDataExclusaoDesc(tenantId);
    }

    @PatchMapping("/{id}/status")
    public void alterarStatus(@PathVariable UUID id, @RequestParam String novoStatus) {
        repository.findById(id).ifPresent(orc -> {
            orc.setStatus(StatusOrcamento.valueOf(novoStatus.toUpperCase()));
            repository.save(orc);
        });
    }

    // RESOLVE O ERRO 404: Mapeia o DELETE para a Lixeira (Soft Delete)
    @DeleteMapping("/{id}")
    public void moverParaLixeira(@PathVariable UUID id) {
        repository.findById(id).ifPresent(orc -> {
            orc.setExcluido(true);
            orc.setDataExclusao(LocalDateTime.now());
            repository.save(orc);
        });
    }

    @PostMapping("/{id}/restaurar")
    public void restaurarDaLixeira(@PathVariable UUID id) {
        repository.findById(id).ifPresent(orc -> {
            orc.setExcluido(false);
            orc.setDataExclusao(null);
            repository.save(orc);
        });
    }
}