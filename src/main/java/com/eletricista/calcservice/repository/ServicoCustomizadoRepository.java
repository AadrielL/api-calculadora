package com.eletricista.calcservice.repository;

import com.eletricista.calcservice.model.ServicoCustomizado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicoCustomizadoRepository extends JpaRepository<ServicoCustomizado, Long> {
    List<ServicoCustomizado> findByTenantIdAndAtivoTrue(String tenantId);
    long countByTenantIdAndAtivoTrue(String tenantId);
}