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
    private Double valor;    // Este é o campo real do preço
    private Boolean ativo = true;

    // Criamos este método para o Service não quebrar,
    // ele simplesmente retorna o conteúdo de 'valor'
    public Double getPrecoBase() {
        return this.valor;
    }
}