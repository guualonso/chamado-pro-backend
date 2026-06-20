package com.chamado.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.chamado.model.enums.CategoriaProblema;
import com.chamado.model.enums.StatusChamado;
import com.chamado.model.enums.Prioridade;
import com.chamado.model.enums.NivelTecnico;
import com.chamado.model.enums.StatusSLA;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor  
@AllArgsConstructor
@Table(name = "chamados")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    private StatusChamado status;

    @Enumerated(EnumType.STRING)
    private CategoriaProblema categoria;

    /**
     * Prioridade do chamado (sugerida automaticamente pela categoria,
     * mas editável). Usada para calcular os prazos de SLA.
     */
    @Enumerated(EnumType.STRING)
    private Prioridade prioridade;

    /**
     * Nível de atendimento atual (N1, N2, N3). Inicia em N1 e
     * sobe via escalonamento manual ou automático (estouro de SLA).
     */
    @Enumerated(EnumType.STRING)
    private NivelTecnico nivelAtual;

    private LocalDateTime dataCriacao;

    private LocalDateTime ultimaAtualizacao;

    /**
     * Momento em que o chamado teve sua primeira interação
     * (ex: atribuição a um técnico ou mudança para EM_ANDAMENTO),
     * usado para medir o SLA de primeira resposta.
     */
    private LocalDateTime dataPrimeiraResposta;

    /**
     * Prazo limite para a primeira resposta, calculado a partir
     * da prioridade do chamado.
     */
    private LocalDateTime prazoPrimeiraResposta;

    /**
     * Prazo limite para a resolução do chamado, calculado a partir
     * da prioridade do chamado.
     */
    private LocalDateTime prazoResolucao;

    /**
     * Situação atual do chamado em relação ao SLA.
     */
    @Enumerated(EnumType.STRING)
    private StatusSLA statusSla;

    private double avaliacao;

    private String feedback;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        if (ultimaAtualizacao == null) {
            ultimaAtualizacao = LocalDateTime.now();
        }
        if (nivelAtual == null) {
            nivelAtual = NivelTecnico.N1;
        }
        if (statusSla == null) {
            statusSla = StatusSLA.NO_PRAZO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ultimaAtualizacao = LocalDateTime.now();
    }
    

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Usuario admin;

    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios = new ArrayList<>();

}