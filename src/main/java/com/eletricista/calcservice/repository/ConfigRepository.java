package com.eletricista.calcservice.repository;

import com.eletricista.calcservice.model.Configuracao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<Configuracao, String> {}