package com.eletricista.calcservice.repository;

import com.eletricista.calcservice.model.Orcamento;
import com.eletricista.calcservice.model.StatusOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {

    // 1. Resolve o erro do HistoricoController
    List<Orcamento> findByTenantIdOrderByDataCriacaoDesc(String tenantId);

    // 2. Resolve o erro da linha [33,44] do DashboardController (o Map de status)
    @Query("SELECT o.status, COUNT(o) FROM Orcamento o WHERE o.tenantId = :tenantId GROUP BY o.status")
    List<Object[]> countStatusByTenant(@Param("tenantId") String tenantId);

    // 3. Resolve o erro da linha [41,34] do DashboardController (contagem de aceitos)
    long countByStatusAndTenantId(StatusOrcamento status, String tenantId);

    // 4. Método base para contagem total
    long countByTenantId(String tenantId);
}