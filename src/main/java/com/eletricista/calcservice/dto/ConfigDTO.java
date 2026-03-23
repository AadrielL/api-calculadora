package com.eletricista.calcservice.dto;

import lombok.Data;

@Data
public class ConfigDTO {
    private Double valorM2ComInfra;
    private Double valorM2SemInfra;
    private Double precoMotor;     // Alinhado com o TS
    private Double precoCamera;    // Alinhado com o TS
    private Double precoCerca;     // Alinhado com o TS
    private Double valorDiaria;
    private Double valorPontoExtra;
    private Double areaBase;
    private Integer pontosBase;
}