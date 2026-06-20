package com.chamado.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.chamado.model.enums.StatusChamado;
import com.chamado.model.enums.CategoriaProblema;
import com.chamado.model.enums.Prioridade;
import com.chamado.model.enums.NivelTecnico;
import com.chamado.model.enums.StatusSLA;

@Getter
@Setter
public class ChamadoDTO {
    private Long id;
    private String titulo;
    private String descricao;
    private StatusChamado status;
    private CategoriaProblema categoria;
    private Prioridade prioridade;
    private NivelTecnico nivelAtual;
    private LocalDateTime dataCriacao;
    private LocalDateTime ultimaAtualizacao;
    private LocalDateTime dataPrimeiraResposta;
    private LocalDateTime prazoPrimeiraResposta;
    private LocalDateTime prazoResolucao;
    private StatusSLA statusSla;
    private double avaliacao;
    private String feedback;
    private Long clienteId;
    private String clienteNome;
    private Long tecnicoId;
    private String tecnicoNome;
    private Long adminId;
    private String adminNome;
}

