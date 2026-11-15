package com.eletricista.calcservice.service;

import com.eletricista.calcservice.dto.*;
import com.eletricista.calcservice.model.*;
import com.eletricista.calcservice.repository.*;
import com.eletricista.calcservice.infra.security.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrcamentoService {

    @Autowired private CalculadoraMaoDeObraService precoService;
    @Autowired private ConfigRepository configRepo;
    @Autowired private OrcamentoRepository orcamentoRepo;
    @Autowired private ObjectMapper objectMapper;

    @Transactional
    public OrcamentoResponse processarNovoOrcamento(QuizRequest quiz, String role) {
        String tenantId = TenantContext.getCurrentTenant();
        String planType = TenantContext.getCurrentPlanType();
        if (planType == null) planType = "FREE";

        java.time.LocalDate today = java.time.LocalDate.now();
        long budgetsToday = orcamentoRepo.countByTenantIdAndDataCriacao(tenantId, today);

        // M2-05: Limite anti-abuso generoso de 15 orçamentos/dia no plano gratuito (FREE)
        if ("FREE".equalsIgnoreCase(planType) && budgetsToday >= 15) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                    "Limite diário anti-abuso (15 orçamentos/dia) atingido para o plano gratuito."
            );
        } else if ("WEEKLY".equalsIgnoreCase(planType) && budgetsToday >= 50) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                    "Limite diário (50) atingido para o plano Semanal."
            );
        } else if (("MONTHLY".equalsIgnoreCase(planType) || "LIFETIME".equalsIgnoreCase(planType) || "PRO".equalsIgnoreCase(planType)) && budgetsToday >= 100) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                    "Limite máximo diário (100) atingido."
            );
        }

        Configuracao conf = configRepo.findById(tenantId).orElse(new Configuracao());
        OrcamentoResponse calculoBase = precoService.calcularOrcamentoCompleto(quiz, conf);

        Orcamento orc = new Orcamento();
        orc.setClienteNome(quiz.nomeCliente());
        orc.setValorTotal(calculoBase.valorTotalMaoDeObra() + calculoBase.custoLogistica());
        orc.setTenantId(tenantId);
        orc.setStatus(StatusOrcamento.PENDENTE);
        orc.setDadosTecnicosSnapshot(converterParaJson(quiz));

        orc = orcamentoRepo.save(orc);

        List<String> listaMensagem;
        String fasesInfo;

        if ("VISITANTE".equalsIgnoreCase(role)) {
            listaMensagem = Collections.singletonList("Disponível apenas para o profissional.");
            fasesInfo = "Protegido por direitos técnicos.";
        } else {
            listaMensagem = Collections.singletonList("Aguardando processamento técnico no módulo Admin.");
            fasesInfo = "Consulte o levantamento técnico na API de Materiais.";
        }

        return new OrcamentoResponse(
                orc.getId(),
                calculoBase.valorTotalMaoDeObra(),
                calculoBase.custoLogistica(),
                listaMensagem,
                fasesInfo,
                orc.getStatus().toString()
        );
    }

    public Orcamento buscarOrcamentoSeguro(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();

        return orcamentoRepo.findById(id)
                .filter(o -> o.getTenantId().equalsIgnoreCase(tenantId))
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado para o usuário: " + tenantId));
    }

    /**
     * M2-02: Busca pública para visualização da proposta pelo cliente do eletricista
     */
    public Orcamento buscarOrcamentoPublico(UUID id) {
        return orcamentoRepo.findById(id)
                .filter(o -> !o.isExcluido())
                .orElseThrow(() -> new RuntimeException("Proposta não encontrada ou indisponível."));
    }

    /**
     * M2-02: Aceite ou Recusa digital do cliente com 1 clique e registro de data/hora no banco
     */
    @Transactional
    public Orcamento registrarAceiteCliente(UUID id, boolean aceitar) {
        Orcamento orc = orcamentoRepo.findById(id)
                .filter(o -> !o.isExcluido())
                .orElseThrow(() -> new RuntimeException("Proposta não encontrada."));

        if (aceitar) {
            orc.setStatus(StatusOrcamento.ACEITO);
            orc.setDataAceite(LocalDateTime.now());
        } else {
            orc.setStatus(StatusOrcamento.RECUSADO);
        }
        return orcamentoRepo.save(orc);
    }

    private String converterParaJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
