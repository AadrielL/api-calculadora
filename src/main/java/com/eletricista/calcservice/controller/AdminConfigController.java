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
@CrossOrigin(origins = "http://localhost:4200")
public class AdminConfigController {

    @Autowired
    private ConfigRepository configRepo;

    @Autowired
    private ServicoCustomizadoRepository servicoRepo;

    /**
     * 1. BUSCAR CONFIGURAÇÃO (GET)
     * Converte a Entity (Banco) para DTO (Angular)
     */
    @GetMapping("/config")
    public ResponseEntity<ConfigDTO> getConfig() {
        String tenantId = TenantContext.getCurrentTenant();

        Configuracao config = configRepo.findById(tenantId)
                .orElse(new Configuracao()); // Retorna padrão se não existir no banco

        // Mapeamento manual: Entity -> DTO
        ConfigDTO dto = new ConfigDTO();
        dto.setValorM2ComInfra(config.getValorM2ComInfra());
        dto.setValorM2SemInfra(config.getValorM2SemInfra());

        // Aqui resolve o problema de sumir no F5:
        dto.setPrecoMotor(config.getPrecoMotorPortao());
        dto.setPrecoCamera(config.getPrecoCamera());
        dto.setPrecoCerca(config.getPrecoCercaMetro());

        dto.setValorDiaria(config.getValorKmRodado());
        dto.setValorPontoExtra(config.getValorPontoExtra());
        dto.setAreaBase(config.getAreaBase());
        dto.setPontosBase(config.getPontosBase());

        return ResponseEntity.ok(dto);
    }

    /**
     * 2. SALVAR/ATUALIZAR CONFIGURAÇÃO (PUT)
     * Converte o DTO (Angular) para Entity (Banco)
     */
    @PutMapping("/config")
    public ResponseEntity<ConfigDTO> atualizarConfig(@RequestBody ConfigDTO dto) {
        String tenantId = TenantContext.getCurrentTenant();
        Configuracao config = configRepo.findById(tenantId).orElse(new Configuracao());

        config.setTenantId(tenantId);
        config.setValorM2ComInfra(dto.getValorM2ComInfra());
        config.setValorM2SemInfra(dto.getValorM2SemInfra());

        // Mapeamento manual: DTO -> Entity
        config.setPrecoMotorPortao(dto.getPrecoMotor());
        config.setPrecoCamera(dto.getPrecoCamera());
        config.setPrecoCercaMetro(dto.getPrecoCerca());

        config.setValorKmRodado(dto.getValorDiaria());
        config.setValorPontoExtra(dto.getValorPontoExtra());
        config.setAreaBase(dto.getAreaBase());
        config.setPontosBase(dto.getPontosBase());

        configRepo.save(config);

        return ResponseEntity.ok(dto);
    }

    /**
     * 3. ADICIONAR SERVIÇO EXTRA (Lustres, Pendentes, etc)
     */
    @PostMapping("/servicos-extras")
    public ResponseEntity<ServicoCustomizado> adicionarServico(@RequestBody ServicoCustomizado servico) {
        String tenantId = TenantContext.getCurrentTenant();
        servico.setTenantId(tenantId);
        servico.setAtivo(true);
        return ResponseEntity.ok(servicoRepo.save(servico));
    }

    /**
     * 4. LISTAR SERVIÇOS EXTRAS
     */
    @GetMapping("/servicos-extras")
    public List<ServicoCustomizado> listarServicos() {
        String tenantId = TenantContext.getCurrentTenant();
        return servicoRepo.findByTenantIdAndAtivoTrue(tenantId);
    }

    /**
     * 5. DELETAR (DESATIVAR) SERVIÇO EXTRA
     */
    @DeleteMapping("/servicos-extras/{id}")
    public ResponseEntity<Void> deletarServico(@PathVariable Long id) {
        String tenantId = TenantContext.getCurrentTenant();
        servicoRepo.findById(id).ifPresent(s -> {
            if(s.getTenantId().equals(tenantId)) {
                s.setAtivo(false); // Soft delete
                servicoRepo.save(s);
            }
        });
        return ResponseEntity.ok().build();
    }
}