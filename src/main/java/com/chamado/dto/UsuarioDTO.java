package com.chamado.dto;

import com.chamado.model.enums.TipoUsuario;
import com.chamado.model.enums.NivelTecnico;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(max = 120)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 254)
    private String email;

    @NotNull
    private TipoUsuario tipoUsuario;

    /** Obrigatória na criação (mín. 8 caracteres); opcional na edição. Nunca é devolvida. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(max = 128)
    private String senha;

    /** Nível de atendimento (N1/N2/N3), apenas para usuários TECNICO. */
    private NivelTecnico nivelTecnico;

    /** Telefone (formato internacional, só dígitos) para notificações via WhatsApp. */
    @Pattern(regexp = "^$|^\\d{10,15}$", message = "deve conter de 10 a 15 dígitos, no formato internacional")
    private String telefone;

    /** API Key do CallMeBot vinculada ao telefone informado. Nunca é devolvida. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(max = 64)
    private String whatsappApiKey;

    /** Somente leitura: o consentimento é dado pelo próprio usuário. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean consentimentoWhatsapp;
}
