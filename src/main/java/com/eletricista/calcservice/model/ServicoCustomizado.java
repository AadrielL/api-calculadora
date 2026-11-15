package com.eletricista.calcservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ServicoCustomizado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tenantId;
    private String nome;
    private String unidade = "UN";
    private Double valor;    // Valor de mão de obra unitário

    @Column(columnDefinition = "TEXT")
    private String materiaisSugeridos; // Lista sugerida de materiais vinculados (M2-01)

    private Boolean ativo = true;

    public Double getPrecoBase() {
        return this.valor;
    }
}
