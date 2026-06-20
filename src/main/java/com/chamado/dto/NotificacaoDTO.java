package com.chamado.dto;

import java.time.LocalDateTime;

import com.chamado.model.enums.CanalNotificacao;
import com.chamado.model.enums.StatusNotificacao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificacaoDTO {
    private Long id;
    private Long chamadoId;
    private String chamadoTitulo;
    private String mensagem;
    private CanalNotificacao canal;
    private StatusNotificacao status;
    private boolean lida;
    private LocalDateTime dataEnvio;
}
