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
        private String nome;     // Ex: "Câmera IP", "Motor Basculante"
        private Double valor;    // Preço unitário da mão de obra
        private Boolean ativo = true;
    }