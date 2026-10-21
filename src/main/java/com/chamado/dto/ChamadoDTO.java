package com.chamado.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
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
    @Size(max = 200)
    private String titulo;
    @Size(max = 5000)
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
    @DecimalMin("0")
    @DecimalMax("5")
    private double avaliacao;
    @Size(max = 2000)
    private String feedback;
    private Long clienteId;
    private String clienteNome;
    private Long tecnicoId;
    private String tecnicoNome;
    private Long adminId;
    private String adminNome;
}

