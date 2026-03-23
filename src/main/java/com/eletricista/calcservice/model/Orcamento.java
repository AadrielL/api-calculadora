package com.eletricista.calcservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Orcamento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String clienteNome;
    private Double valorTotal;
    private String tenantId;
    private LocalDate dataCriacao = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private StatusOrcamento status;

    // Snapshot: Armazena o resultado do levantamento como JSON
    @Column(columnDefinition = "TEXT")
    private String dadosTecnicosSnapshot;

    // --- CAMPOS PARA LIXEIRA (7 DIAS) ---
    private boolean excluido = false;
    private LocalDateTime dataExclusao;
}