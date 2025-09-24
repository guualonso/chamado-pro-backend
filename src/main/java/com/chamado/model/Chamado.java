package com.chamado.model;

import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;

import com.chamado.model.enums.CategoriaProblema;
import com.chamado.model.enums.StatusChamado;

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

    private LocalDateTime dataCriacao;

    private LocalDateTime ultimaAtualizacao;

    // Cliente que abriu o chamado (OBRIGATÓRIO para abrir chamado)
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
    }

    @PreUpdate
    protected void onUpdate() {
        ultimaAtualizacao = LocalDateTime.now();
    }
    
    // Técnico responsável - PARA IMPLEMENTAÇÃO FUTURA
    // @ManyToOne
    // @JoinColumn(name = "tecnico_id")
    // private Usuario tecnico;

    // Admin responsável - PARA IMPLEMENTAÇÃO FUTURA  
    // @ManyToOne
    // @JoinColumn(name = "admin_id")
    // private Usuario admin;

    // Comentários - PARA IMPLEMENTAÇÃO FUTURA
    // @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Comentario> comentarios = new ArrayList<>();

}