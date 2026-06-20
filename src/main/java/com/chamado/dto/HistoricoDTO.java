package com.chamado.dto;

import java.time.LocalDateTime;

import com.chamado.model.enums.TipoEventoHistorico;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoricoDTO {
    private Long id;
    private Long chamadoId;
    private String usuarioNome;
    private TipoEventoHistorico tipoEvento;
    private String descricao;
    private LocalDateTime dataHora;
}
