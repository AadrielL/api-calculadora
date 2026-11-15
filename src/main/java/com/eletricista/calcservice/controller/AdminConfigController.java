package com.eletricista.calcservice.controller;

import com.eletricista.calcservice.infra.security.tenant.TenantContext;
import com.eletricista.calcservice.dto.ConfigDTO;
import com.eletricista.calcservice.model.Configuracao;
import com.eletricista.calcservice.model.ServicoCustomizado;
import com.eletricista.calcservice.repository.ConfigRepository;
import com.eletricista.calcservice.repository.ServicoCustomizadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin")
@CrossOrigin(originPatterns = "*", allowCredentials = "true", allowedHeaders = "*")
public class AdminConfigController {

    @Autowired
    private ConfigRepository configRepo;

    @Autowired
    private ServicoCustomizadoRepository servicoRepo;

    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        String tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null || tenantId.isEmpty()) {
            return ResponseEntity.status(401).body("Tenant ID não identificado no Token.");
        }

        Configuracao config = configRepo.findById(tenantId)
                .orElse(new Configuracao());

        ConfigDTO dto = new ConfigDTO();
        dto.setValorM2ComInfra(config.getValorM2ComInfra());
        dto.setValorM2SemInfra(config.getValorM2SemInfra());
        dto.setPrecoMotor(config.getPrecoMotorPortao());
        dto.setPrecoCamera(config.getPrecoCamera());
        dto.setPrecoCerca(config.getPrecoCercaMetro());
        dto.setValorDiaria(config.getValorKmRodado());
        dto.setValorPontoExtra(config.getValorPontoExtra());
        dto.setAreaBase(config.getAreaBase());
        dto.setPontosBase(config.getPontosBase());
        dto.setTelefoneDistribuidora(
                config.getTelefoneDistribuidora() != null && !config.getTelefoneDistribuidora().isBlank()
                        ? config.getTelefoneDistribuidora()
                        : "556333222120"
        );
        dto.setNomeDistribuidora(
                config.getNomeDistribuidora() != null && !config.getNomeDistribuidora().isBlank()
                        ? config.getNomeDistribuidora()
                        : "Capital Elétrica - Palmas/TO"
        );

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/config")
    public ResponseEntity<?> atualizarConfig(@RequestBody ConfigDTO dto) {
        String tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null || tenantId.isEmpty()) {
            return ResponseEntity.status(401).body("Tenant ID não identificado.");
        }

        Configuracao config = configRepo.findById(tenantId).orElse(new Configuracao());

        config.setTenantId(tenantId);
        config.setValorM2ComInfra(Math.max(0.0, dto.getValorM2ComInfra() != null ? dto.getValorM2ComInfra() : 0.0));
        config.setValorM2SemInfra(Math.max(0.0, dto.getValorM2SemInfra() != null ? dto.getValorM2SemInfra() : 0.0));
        config.setPrecoMotorPortao(Math.max(0.0, dto.getPrecoMotor() != null ? dto.getPrecoMotor() : 0.0));
        config.setPrecoCamera(Math.max(0.0, dto.getPrecoCamera() != null ? dto.getPrecoCamera() : 0.0));
        config.setPrecoCercaMetro(Math.max(0.0, dto.getPrecoCerca() != null ? dto.getPrecoCerca() : 0.0));
        config.setValorKmRodado(Math.max(0.0, dto.getValorDiaria() != null ? dto.getValorDiaria() : 0.0));
        config.setValorPontoExtra(Math.max(0.0, dto.getValorPontoExtra() != null ? dto.getValorPontoExtra() : 0.0));
        config.setAreaBase(dto.getAreaBase() != null ? dto.getAreaBase() : 100.0);
        config.setPontosBase(dto.getPontosBase() != null ? dto.getPontosBase() : 30);

        if (dto.getTelefoneDistribuidora() != null && !dto.getTelefoneDistribuidora().isBlank()) {
            config.setTelefoneDistribuidora(dto.getTelefoneDistribuidora().replaceAll("\\D", ""));
        }
        if (dto.getNomeDistribuidora() != null && !dto.getNomeDistribuidora().isBlank()) {
            config.setNomeDistribuidora(dto.getNomeDistribuidora().trim());
        }

        configRepo.save(config);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/servicos-extras")
    public ResponseEntity<?> adicionarServico(@RequestBody ServicoCustomizado servico) {
        String tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        if (servico.getValor() == null || servico.getValor() < 0) {
            servico.setValor(0.0);
        }
        if (servico.getUnidade() == null || servico.getUnidade().isBlank()) {
            servico.setUnidade("UN");
        }

        servico.setTenantId(tenantId);
        servico.setAtivo(true);
        return ResponseEntity.ok(servicoRepo.save(servico));
    }

    @GetMapping("/servicos-extras")
    public List<ServicoCustomizado> listarServicos() {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) return List.of();

        return servicoRepo.findByTenantIdAndAtivoTrue(tenantId);
    }

    @DeleteMapping("/servicos-extras/{id}")
    public ResponseEntity<Void> deletarServico(@PathVariable Long id) {
        String tenantId = TenantContext.getCurrentTenant();

        if (tenantId != null) {
            servicoRepo.findById(id).ifPresent(s -> {
                if (s.getTenantId().equals(tenantId)) {
                    s.setAtivo(false);
                    servicoRepo.save(s);
                }
            });
        }
        return ResponseEntity.ok().build();
    }
}
