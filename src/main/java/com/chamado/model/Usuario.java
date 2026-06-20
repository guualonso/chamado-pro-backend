package com.chamado.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.chamado.model.enums.TipoUsuario;
import com.chamado.model.enums.NivelTecnico;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String nome;
    
    @Column(nullable = false)
    private String senha;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipoUsuario;

    /**
     * Nível de atendimento do técnico (N1, N2 ou N3).
     * Só é relevante quando tipoUsuario = TECNICO, usado no escalonamento.
     */
    @Enumerated(EnumType.STRING)
    private NivelTecnico nivelTecnico;

    /**
     * Telefone no formato internacional (ex: 5511999999999),
     * usado para envio de notificações via WhatsApp (CallMeBot).
     */
    private String telefone;

    /**
     * API Key do CallMeBot vinculada ao número de telefone acima.
     * Necessária para o envio real de mensagens via WhatsApp.
     */
    private String whatsappApiKey;

    // Getters e Setters
}