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
@CrossOrigin(originPatterns = "*", allowCredentials = "true", allowedHeaders = "*")
public class HistoricoController {

    private final OrcamentoRepository repository;

    public HistoricoController(OrcamentoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Orcamento> listarHistorico() {
        String tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndExcluidoFalseOrderByDataCriacaoDesc(tenantId);
    }

    @GetMapping("/lixeira")
    public List<Orcamento> listarLixeira() {
        String tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndExcluidoTrueOrderByDataExclusaoDesc(tenantId);
    }

    @PatchMapping("/{id}/status")
    public void alterarStatus(@PathVariable UUID id, @RequestParam String novoStatus) {
        repository.findById(id).ifPresent(orc -> {
            StatusOrcamento statusEnum = StatusOrcamento.valueOf(novoStatus.toUpperCase());
            orc.setStatus(statusEnum);
            if (statusEnum == StatusOrcamento.ACEITO) {
                orc.setDataAceite(LocalDateTime.now());
            } else if (statusEnum == StatusOrcamento.ENVIADO) {
                orc.setDataEnvio(LocalDateTime.now());
            }
            repository.save(orc);
        });
    }

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
