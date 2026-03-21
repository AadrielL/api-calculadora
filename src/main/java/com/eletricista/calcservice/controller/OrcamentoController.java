package com.eletricista.calcservice.controller;

import com.eletricista.calcservice.config.TenantContext;
import com.eletricista.calcservice.dto.*;
import com.eletricista.calcservice.model.*;
import com.eletricista.calcservice.service.*;
import com.eletricista.calcservice.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/orcamentos")
@CrossOrigin(origins = "http://localhost:4200")
public class OrcamentoController {

    @Autowired private CalculadoraPrecoService precoService;
    @Autowired private MaterialService materialService;
    @Autowired private ConfigRepository configRepo;
    @Autowired private OrcamentoRepository orcamentoRepo;
    @Autowired private ObjectMapper objectMapper;

    @PostMapping("/gerar-quiz")
    public OrcamentoResponse gerar(@RequestBody QuizRequest quiz,
                                   @RequestHeader(value = "X-User-Role", defaultValue = "VISITANTE") String role) {

        String tenantId = TenantContext.getCurrentTenant();
        Configuracao conf = configRepo.findById(tenantId).orElse(new Configuracao());

        OrcamentoResponse calculoBase = precoService.calcularOrcamentoCompleto(quiz, conf);

        Orcamento orc = new Orcamento();
        orc.setClienteNome(quiz.nomeCliente());
        orc.setValorTotal(calculoBase.valorTotalMaoDeObra() + calculoBase.custoLogistica());
        orc.setTenantId(tenantId);
        orc.setStatus(StatusOrcamento.PENDENTE_ADMIN);

        try {
            orc.setDadosTecnicosSnapshot(objectMapper.writeValueAsString(quiz));
        } catch (Exception e) {
            orc.setDadosTecnicosSnapshot("{}");
        }

        orc = orcamentoRepo.save(orc);

        List<String> listaTecnica = materialService.gerarListaMateriais(quiz);
        String fases = materialService.sugerirEquilibrioFases();

        if ("VISITANTE".equalsIgnoreCase(role)) {
            listaTecnica = Collections.singletonList("Disponível apenas para o profissional.");
            fases = "Protegido por direitos técnicos.";
        }

        return new OrcamentoResponse(
                orc.getId(),
                calculoBase.valorTotalMaoDeObra(),
                calculoBase.custoLogistica(),
                listaTecnica,
                fases,
                orc.getStatus().toString()
        );
    }

    @GetMapping("/{id}")
    public Orcamento buscarPorId(@PathVariable UUID id) {
        // Pega o Tenant que o Interceptor já validou
        String tenantId = TenantContext.getCurrentTenant();

        return orcamentoRepo.findById(id)
                .filter(o -> o.getTenantId().equalsIgnoreCase(tenantId))
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado para o usuário: " + tenantId));
    }
}