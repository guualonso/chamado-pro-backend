package com.chamado.service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.chamado.dto.DashboardDTO;
import com.chamado.model.Chamado;
import com.chamado.model.enums.NivelTecnico;
import com.chamado.model.enums.StatusChamado;
import com.chamado.model.enums.StatusSLA;
import com.chamado.repository.ChamadoRepository;

/**
 * Calcula as métricas agregadas exibidas no painel administrativo:
 * totais por status, situação do SLA, distribuição por categoria/prioridade/
 * nível/técnico e tempo médio de resolução.
 */
@Service
public class DashboardService {

    private final ChamadoRepository chamadoRepository;

    public DashboardService(ChamadoRepository chamadoRepository) {
        this.chamadoRepository = chamadoRepository;
    }

    public DashboardDTO obterDados() {
        List<Chamado> todos = chamadoRepository.findAll();

        long total = todos.size();
        long abertos = contarPorStatus(todos, StatusChamado.ABERTO);
        long emAndamento = contarPorStatus(todos, StatusChamado.EM_ANDAMENTO);
        long resolvidos = contarPorStatus(todos, StatusChamado.RESOLVIDO);
        long fechados = contarPorStatus(todos, StatusChamado.FECHADO);

        long escalonados = todos.stream()
                .filter(c -> c.getNivelAtual() != null && c.getNivelAtual() != NivelTecnico.N1)
                .count();

        long slaNoPrazo = contarPorSla(todos, StatusSLA.NO_PRAZO);
        long slaEmRisco = contarPorSla(todos, StatusSLA.EM_RISCO);
        long slaEstourado = contarPorSla(todos, StatusSLA.ESTOURADO);
        long slaCumprido = contarPorSla(todos, StatusSLA.CUMPRIDO);

        long baseCalculo = slaCumprido + slaEstourado;
        double percentualSlaCumprido = baseCalculo > 0 ? (slaCumprido * 100.0 / baseCalculo) : 100.0;

        double tempoMedioResolucaoHoras = todos.stream()
                .filter(c -> (c.getStatus() == StatusChamado.RESOLVIDO || c.getStatus() == StatusChamado.FECHADO)
                        && c.getDataCriacao() != null && c.getUltimaAtualizacao() != null)
                .mapToLong(c -> Duration.between(c.getDataCriacao(), c.getUltimaAtualizacao()).toMinutes())
                .average()
                .orElse(0.0) / 60.0;

        Map<String, Long> porCategoria = todos.stream()
                .filter(c -> c.getCategoria() != null)
                .collect(Collectors.groupingBy(c -> c.getCategoria().name(), LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> porPrioridade = todos.stream()
                .filter(c -> c.getPrioridade() != null)
                .collect(Collectors.groupingBy(c -> c.getPrioridade().name(), LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> porNivel = todos.stream()
                .filter(c -> c.getNivelAtual() != null)
                .collect(Collectors.groupingBy(c -> c.getNivelAtual().name(), LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> porTecnico = todos.stream()
                .filter(c -> c.getTecnico() != null)
                .collect(Collectors.groupingBy(c -> c.getTecnico().getNome(), LinkedHashMap::new, Collectors.counting()));

        return new DashboardDTO(
                total,
                abertos,
                emAndamento,
                resolvidos,
                fechados,
                escalonados,
                slaNoPrazo,
                slaEmRisco,
                slaEstourado,
                slaCumprido,
                percentualSlaCumprido,
                tempoMedioResolucaoHoras,
                porCategoria,
                porPrioridade,
                porNivel,
                porTecnico
        );
    }

    private long contarPorStatus(List<Chamado> chamados, StatusChamado status) {
        return chamados.stream().filter(c -> c.getStatus() == status).count();
    }

    private long contarPorSla(List<Chamado> chamados, StatusSLA status) {
        return chamados.stream().filter(c -> c.getStatusSla() == status).count();
    }
}
