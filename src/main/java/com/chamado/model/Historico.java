package com.chamado.model;

import java.time.LocalDateTime;

import com.chamado.model.enums.TipoEventoHistorico;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro de histórico/auditoria das mudanças ocorridas em um chamado
 * (mudança de status, prioridade, atribuição, escalonamento, etc).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "historico_chamado")
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    /**
     * Usuário responsável pela ação. Pode ser nulo quando o evento
     * foi gerado automaticamente pelo sistema (ex: escalonamento por SLA).
     */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEventoHistorico tipoEvento;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private LocalDateTime dataHora;

    @PrePersist
    protected void onCreate() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }
}
