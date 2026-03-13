package com.eletricista.calcservice.repository;

import com.eletricista.calcservice.model.Orcamento;
import com.eletricista.calcservice.model.StatusOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {

    // ADICIONE ESTA LINHA: É ela que o HistoricoController está pedindo
    List<Orcamento> findByTenantIdOrderByDataCriacaoDesc(String tenantId);

    @Query("SELECT o.status, COUNT(o) FROM Orcamento o WHERE o.tenantId = :tenantId GROUP BY o.status")
    List<Object[]> countStatusByTenant(@Param("tenantId") String tenantId);

    long countByTenantId(String tenantId);

    long countByStatusAndTenantId(StatusOrcamento status, String tenantId);
}