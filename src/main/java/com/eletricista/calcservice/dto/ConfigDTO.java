package com.eletricista.calcservice.dto;

import lombok.Data;

@Data
public class ConfigDTO {
    private Double valorM2ComInfra;
    private Double valorM2SemInfra;
    private Double precoMotor;
    private Double precoCamera;
    private Double precoCerca;
    private Double valorDiaria;
    private Double valorPontoExtra;
    private Double areaBase;
    private Integer pontosBase;
    private String telefoneDistribuidora;
    private String nomeDistribuidora;
}
