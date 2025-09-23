package com.chamado.model;


import java.time.LocalDateTime;
import java.util.List;

import com.chamado.model.enums.CategoriaProblema;
import com.chamado.model.enums.StatusChamado;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
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

    private LocalDateTime dataCriacao;

    private LocalDateTime ultimaAtualizacao;

    // Cliente que abriu o chamado
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    // Técnico responsável (pode ser null inicialmente)
    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    // Admin responsável (opcional)
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Usuario admin;

    // Comentários
    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios;

    // Getters e Setters
}