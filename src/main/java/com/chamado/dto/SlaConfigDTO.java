package com.chamado.dto;

import com.chamado.model.enums.Prioridade;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlaConfigDTO {
    private Long id;
    private Prioridade prioridade;
    private Integer tempoRespostaMinutos;
    private Integer tempoResolucaoMinutos;
}
