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
@CrossOrigin(origins = "http://localhost:4200")
public class AdminConfigController {

    @Autowired private ConfigRepository configRepo;
    @Autowired private ServicoCustomizadoRepository servicoRepo;

    // 1. Atualiza ou Cria as configurações de preços base
    @PutMapping("/config")
    public ResponseEntity<Configuracao> atualizarConfig(@RequestBody Configuracao novaConfig) {
        String tenantId = TenantContext.getCurrentTenant();

        // Busca se já existe uma config para este eletricista, se não, cria uma nova
        Configuracao configExistente = configRepo.findById(tenantId).orElse(new Configuracao());

        // Atualiza apenas os campos necessários (Exemplos baseado no seu Service)
        configExistente.setTenantId(tenantId);
        configExistente.setValorM2ComInfra(novaConfig.getValorM2ComInfra());
        configExistente.setValorM2SemInfra(novaConfig.getValorM2SemInfra());
        configExistente.setPrecoInstalacaoChuveiro(novaConfig.getPrecoInstalacaoChuveiro());
        configExistente.setPrecoInstalacaoAr(novaConfig.getPrecoInstalacaoAr());
        configExistente.setValorPontoExtra(novaConfig.getValorPontoExtra());
        // Adicione aqui os demais campos (diária, gasolina, fatores de luxo...)

        return ResponseEntity.ok(configRepo.save(configExistente));
    }

    // 2. Adiciona um novo serviço customizado (Max 10)
    @PostMapping("/servicos-extras")
    public ResponseEntity<?> adicionarServico(@RequestBody ServicoCustomizado servico) {
        String tenantId = TenantContext.getCurrentTenant();

        long totalAtual = servicoRepo.countByTenantIdAndAtivoTrue(tenantId);
        if (totalAtual >= 10) {
            return ResponseEntity.badRequest().body("Limite de 10 serviços customizados atingido.");
        }

        servico.setTenantId(tenantId);
        servico.setAtivo(true);
        return ResponseEntity.ok(servicoRepo.save(servico));
    }

    // 3. Busca a configuração atual (Para carregar no formulário do Admin)
    @GetMapping("/config")
    public ResponseEntity<Configuracao> getConfig() {
        String tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(configRepo.findById(tenantId).orElse(new Configuracao()));
    }

    @GetMapping("/servicos-extras")
    public List<ServicoCustomizado> listarServicos() {
        return servicoRepo.findByTenantIdAndAtivoTrue(TenantContext.getCurrentTenant());
    }

    @DeleteMapping("/servicos-extras/{id}")
    public ResponseEntity<?> deletarServico(@PathVariable Long id) {
        String tenantId = TenantContext.getCurrentTenant();
        servicoRepo.findById(id).ifPresent(s -> {
            if(s.getTenantId().equals(tenantId)) {
                s.setAtivo(false); // Soft delete é melhor que delete físico
                servicoRepo.save(s);
            }
        });
        return ResponseEntity.ok().build();
    }
}