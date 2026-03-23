package com.eletricista.calcservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemAdicionalDTO(
        String nomeServico,    // Bate com o log: 'nomeServico'
        Double precoUnitario,  // Bate com o log: 'precoUnitario'
        Integer quantidade,
        Boolean selecionado
) {
    // Método para garantir que o nome nunca venha nulo no log do Java
    public String getDescricao() {
        return nomeServico != null ? nomeServico : "Adicional Sem Nome";
    }
}