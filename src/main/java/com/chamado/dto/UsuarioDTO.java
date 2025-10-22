package com.chamado.dto;

import com.chamado.model.enums.TipoUsuario;
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
    private String senha; // remover quando autenticação for implementada (authcontroller)
    private TipoUsuario tipoUsuario;
}
