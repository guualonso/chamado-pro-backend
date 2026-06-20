package com.chamado.model;

import com.chamado.model.enums.Prioridade;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuração de SLA (prazos de primeira resposta e resolução)
 * por nível de prioridade do chamado. Editável pelo administrador.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sla_config")
public class SlaConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Prioridade prioridade;

    /**
     * Tempo máximo (em minutos) para a primeira resposta ao chamado.
     */
    @Column(nullable = false)
    private Integer tempoRespostaMinutos;

    /**
     * Tempo máximo (em minutos) para a resolução completa do chamado.
     */
    @Column(nullable = false)
    private Integer tempoResolucaoMinutos;
}
