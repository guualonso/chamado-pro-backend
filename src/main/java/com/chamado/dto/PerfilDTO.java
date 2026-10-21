package com.chamado.dto;

import java.time.LocalDateTime;

import com.chamado.model.enums.NivelTecnico;
import com.chamado.model.enums.TipoUsuario;

import lombok.Getter;
import lombok.Setter;

/**
 * Dados do próprio usuário autenticado (GET /usuarios/me), incluindo
 * a situação dos consentimentos exigidos pela LGPD.
 */
@Getter
@Setter
public class PerfilDTO {
    private Long id;
    private String nome;
    private String email;
    private TipoUsuario tipoUsuario;
    private NivelTecnico nivelTecnico;
    private String telefone;
    private boolean whatsappConfigurado;
    private boolean consentimentoWhatsapp;
    private LocalDateTime dataConsentimentoWhatsapp;
    private String versaoTermosAceita;
    private LocalDateTime dataAceiteTermos;
    private String versaoTermosAtual;
}
