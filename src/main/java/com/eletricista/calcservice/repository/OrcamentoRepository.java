package com.eletricista.calcservice.repository;

import com.eletricista.calcservice.model.Orcamento;
import com.eletricista.calcservice.model.StatusOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {

    // Métodos para o Histórico e Lixeira
    List<Orcamento> findByTenantIdAndExcluidoFalseOrderByDataCriacaoDesc(String tenantId);
    List<Orcamento> findByTenantIdAndExcluidoTrueOrderByDataExclusaoDesc(String tenantId);

    // Métodos para a Dashboard
    long countByTenantIdAndExcluidoFalse(String tenantId);

    long countByStatusAndTenantIdAndExcluidoFalse(StatusOrcamento status, String tenantId);

    @Query("SELECT o.status, COUNT(o) FROM Orcamento o WHERE o.tenantId = :tenantId AND o.excluido = false GROUP BY o.status")
    List<Object[]> countStatusByTenant(@Param("tenantId") String tenantId);

    // CORREÇÃO AQUI: As anotações devem ficar no método de escrita
    @Modifying
    @Transactional
    void deleteByExcluidoTrueAndDataExclusaoBefore(LocalDateTime dataLimite);
}