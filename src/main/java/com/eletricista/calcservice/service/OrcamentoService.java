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