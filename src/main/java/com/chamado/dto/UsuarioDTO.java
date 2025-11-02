package com.chamado.dto;

import com.chamado.model.enums.TipoUsuario;
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
}
