package com.chamado.model;

import java.time.LocalDateTime;

import com.chamado.model.enums.CanalNotificacao;
import com.chamado.model.enums.StatusNotificacao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Notificação enviada (ou que tentou ser enviada) a um usuário,
 * seja pelo painel do sistema, seja via WhatsApp (CallMeBot).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notificacoes")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @ManyToOne
    @JoinColumn(name = "chamado_id")
    private Chamado chamado;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalNotificacao canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusNotificacao status;

    @Column(nullable = false)
    private boolean lida = false;

    private LocalDateTime dataEnvio;

    @PrePersist
    protected void onCreate() {
        if (dataEnvio == null) {
            dataEnvio = LocalDateTime.now();
        }
    }
}
