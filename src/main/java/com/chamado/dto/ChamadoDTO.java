package com.chamado.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.chamado.model.enums.StatusChamado;

@Getter
@Setter
public class ChamadoDTO {
    private Long id;
    private String titulo;
    private String descricao;
    private StatusChamado status;
    private LocalDateTime dataCriacao;
    private LocalDateTime ultimaAtualizacao;
    private String clienteNome;
    private String tecnicoNome;
    private String adminNome;

    // Getters e Setters
}
