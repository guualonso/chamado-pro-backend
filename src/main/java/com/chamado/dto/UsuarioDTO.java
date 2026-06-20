package com.chamado.dto;

import com.chamado.model.enums.TipoUsuario;
import com.chamado.model.enums.NivelTecnico;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;
    private String nome;
    private String email;
    private TipoUsuario tipoUsuario;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    /** Nível de atendimento (N1/N2/N3), apenas para usuários TECNICO. */
    private NivelTecnico nivelTecnico;

    /** Telefone (formato internacional) para notificações via WhatsApp. */
    private String telefone;

    /** API Key do CallMeBot vinculada ao telefone informado. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String whatsappApiKey;
}

