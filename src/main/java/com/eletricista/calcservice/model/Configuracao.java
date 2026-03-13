package com.eletricista.calcservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Configuracao {
    @Id
    private String tenantId;

    // Novos campos de Infra
    private Double valorM2ComInfra = 35.0;
    private Double valorM2SemInfra = 18.0;

    // Regra de Crescimento (100m² = 30 pontos + 2 a cada 5m²)
    private Double areaBase = 100.0;
    private Integer pontosBase = 30;
    private Double stepMetragem = 5.0;
    private Integer ganhoPontosPorStep = 2;

    // Valores Unitários Técnicos
    private Double valorPontoExtra = 55.0;
    private Double precoInstalacaoChuveiro = 80.0;
    private Double precoInstalacaoAr = 150.0;

    private Double valorKmRodado = 2.5;
    private Double precoCamera = 150.0;
    private Double precoMotorPortao = 350.0;
    private Double precoCercaMetro = 25.0;
    private Double fatorReforma = 1.3;
    private Double fatorAltoPadrao = 1.6;
}