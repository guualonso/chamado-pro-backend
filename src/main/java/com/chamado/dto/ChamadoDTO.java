package com.chamado.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.chamado.model.enums.StatusChamado;
import com.chamado.model.enums.CategoriaProblema;

@Getter
@Setter
public class ChamadoDTO {
    private Long id;
    private String titulo;
    private String descricao;
    private StatusChamado status;
    private CategoriaProblema categoria;
    private LocalDateTime dataCriacao;
    private LocalDateTime ultimaAtualizacao;
    private double avaliacao;
    private String feedback;
    private Long clienteId;
    private String clienteNome;
    private Long tecnicoId;
    private String tecnicoNome;
    private Long adminId;
    private String adminNome;
}
