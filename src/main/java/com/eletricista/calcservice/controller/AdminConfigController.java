package com.eletricista.calcservice.controller;

import com.eletricista.calcservice.config.TenantContext;
import com.eletricista.calcservice.model.*;
import com.eletricista.calcservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/admin")
public class AdminConfigController {

    @Autowired private ConfigRepository configRepo;
    @Autowired private ServicoCustomizadoRepository servicoRepo;

    // 1. Atualiza as configurações padrão (Valores de m2, Chuveiro, etc)
    @PutMapping("/config")
    public Configuracao atualizarConfig(@RequestBody Configuracao novaConfig) {
        novaConfig.setTenantId(TenantContext.getCurrentTenant());
        return configRepo.save(novaConfig);
    }

    // 2. Adiciona um novo serviço (Ex: Lustre) com limite de 10
    @PostMapping("/servicos-extras")
    public ResponseEntity<?> adicionarServico(@RequestBody ServicoCustomizado servico) {
        String tenantId = TenantContext.getCurrentTenant();

        // Regra de Negócio: Limite de 10 serviços por eletricista
        long totalAtual = servicoRepo.countByTenantIdAndAtivoTrue(tenantId);
        if (totalAtual >= 10) {
            return ResponseEntity.badRequest().body("Limite de 10 serviços customizados atingido.");
        }

        servico.setTenantId(tenantId);
        return ResponseEntity.ok(servicoRepo.save(servico));
    }

    // 3. Lista os serviços para o Quiz carregar as opções
    @GetMapping("/servicos-extras")
    public List<ServicoCustomizado> listarServicos() {
        return servicoRepo.findByTenantIdAndAtivoTrue(TenantContext.getCurrentTenant());
    }

    // 4. Deleta/Inativa um serviço
    @DeleteMapping("/servicos-extras/{id}")
    public void deletarServico(@PathVariable Long id) {
        servicoRepo.deleteById(id);
    }
}