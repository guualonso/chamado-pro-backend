package com.chamado.model.enums;

/**
 * Tipos de evento registrados no histórico de um chamado.
 */
public enum TipoEventoHistorico {
    CRIACAO,
    MUDANCA_STATUS,
    MUDANCA_PRIORIDADE,
    ATRIBUICAO,
    ESCALONAMENTO,
    COMENTARIO,
    ALERTA_SLA
}
