package com.eletricista.calcservice.repository;

import com.eletricista.calcservice.model.ServicoCustomizado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicoCustomizadoRepository extends JpaRepository<ServicoCustomizado, Long> {

    // Este método resolve o erro do CalculadoraPrecoService
    List<ServicoCustomizado> findByTenantIdAndAtivoTrue(String tenantId);

    // Este método servirá para a nossa trava de limite de 10 serviços
    long countByTenantIdAndAtivoTrue(String tenantId);
}