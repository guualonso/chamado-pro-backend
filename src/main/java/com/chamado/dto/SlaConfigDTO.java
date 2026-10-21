package com.chamado.dto;

import com.chamado.model.enums.Prioridade;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlaConfigDTO {
    private Long id;
    private Prioridade prioridade;
    @Min(1)
    @Max(525600) // 1 ano
    private Integer tempoRespostaMinutos;
    @Min(1)
    @Max(525600)
    private Integer tempoResolucaoMinutos;
}
