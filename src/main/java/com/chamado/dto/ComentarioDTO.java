package com.chamado.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComentarioDTO {
    private Long id;
    @NotBlank
    @Size(max = 5000)
    private String texto;
    private LocalDateTime dataCriacao;
    /** Preenchido pelo servidor a partir do usuário autenticado (não pode ser forjado). */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String autorNome;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long autorId;
    private Long chamadoId;
}
