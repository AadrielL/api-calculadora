package com.eletricista.calcservice.service;

import com.eletricista.calcservice.dto.*;
import com.eletricista.calcservice.model.*;
import com.eletricista.calcservice.repository.*;
import com.eletricista.calcservice.infra.security.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime endOfDay = java.time.LocalDate.now().atTime(23, 59, 59);
        long budgetsToday = orcamentoRepo.countByTenantIdAndDataCriacaoBetween(tenantId, startOfDay, endOfDay);

        if ("FREE".equals(planType) && budgetsToday >= 3) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "Limite diário de orçamentos (3) atingido para usuários gratuitos.");
        } else if ("WEEKLY".equals(planType) && budgetsToday >= 50) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "Limite diário (50) atingido para o plano Semanal.");
        } else if (("MONTHLY".equals(planType) || "LIFETIME".equals(planType)) && budgetsToday >= 100) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "Limite máximo diário (100) atingido.");
        }

        // 1. Busca Configurações do banco (ou usa default se não existir)
        Configuracao conf = configRepo.findById(tenantId).orElse(new Configuracao());

        // 2. Calcula Mão de Obra e Logística através do Service Especialista
        OrcamentoResponse calculoBase = precoService.calcularOrcamentoCompleto(quiz, conf);

        // 3. Monta e Salva a Entidade Orcamento (O "Contrato" entre APIs)
        Orcamento orc = new Orcamento();
        orc.setClienteNome(quiz.nomeCliente());
        orc.setValorTotal(calculoBase.valorTotalMaoDeObra() + calculoBase.custoLogistica());
        orc.setTenantId(tenantId);
        orc.setStatus(StatusOrcamento.PENDENTE_ADMIN); // Admin verá isso na outra API

        orc.setDadosTecnicosSnapshot(converterParaJson(quiz));

        orc = orcamentoRepo.save(orc);

        // 4. Definição de Mensagens de Resposta (Substituindo o antigo MaterialService)
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

    /**
     * Busca um orçamento garantindo que ele pertença ao Tenant logado.
     */
    public Orcamento buscarOrcamentoSeguro(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();

        return orcamentoRepo.findById(id)
                .filter(o -> o.getTenantId().equalsIgnoreCase(tenantId))
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado para o usuário: " + tenantId));
    }

    private String converterParaJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}