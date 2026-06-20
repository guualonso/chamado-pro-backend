package com.chamado.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Conjunto de métricas agregadas exibidas no dashboard do administrador.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    // Totais por status
    private long totalChamados;
    private long chamadosAbertos;
    private long chamadosEmAndamento;
    private long chamadosResolvidos;
    private long chamadosFechados;

    // Escalonamento
    private long chamadosEscalonados;

    // SLA
    private long slaNoPrazo;
    private long slaEmRisco;
    private long slaEstourado;
    private long slaCumprido;
    private double percentualSlaCumprido;

    // Tempo médio de resolução (em horas)
    private double tempoMedioResolucaoHoras;

    // Distribuições para gráficos
    private Map<String, Long> chamadosPorCategoria;
    private Map<String, Long> chamadosPorPrioridade;
    private Map<String, Long> chamadosPorNivel;
    private Map<String, Long> chamadosPorTecnico;
}
