package com.eletricista.calcservice.service;

import com.eletricista.calcservice.repository.OrcamentoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
public class PurgeLixeiraTask {

    private final OrcamentoRepository repository;

    public PurgeLixeiraTask(OrcamentoRepository repository) {
        this.repository = repository;
    }

    // Roda todos os dias à meia-noite
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deletarOrcamentosAntigosDaLixeira() {
        LocalDateTime limite = LocalDateTime.now().minusDays(7);

        // No Repository, precisaremos deste método para a exclusão física
        repository.deleteByExcluidoTrueAndDataExclusaoBefore(limite);

        System.out.println("Faxina concluída: Itens com mais de 7 dias removidos da lixeira.");
    }
}