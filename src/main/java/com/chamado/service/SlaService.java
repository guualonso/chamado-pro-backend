package com.chamado.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.chamado.dto.SlaConfigDTO;
import com.chamado.model.Chamado;
import com.chamado.model.SlaConfig;
import com.chamado.model.enums.CategoriaProblema;
import com.chamado.model.enums.Prioridade;
import com.chamado.model.enums.StatusChamado;
import com.chamado.model.enums.StatusSLA;
import com.chamado.repository.SlaConfigRepository;

/**
 * Centraliza as regras de SLA: sugestão de prioridade, cálculo dos prazos
 * de primeira resposta/resolução e cálculo do status atual do SLA.
 */
@Service
public class SlaService {

    private final SlaConfigRepository slaConfigRepository;

    public SlaService(SlaConfigRepository slaConfigRepository) {
        this.slaConfigRepository = slaConfigRepository;
    }

    /**
     * Sugere uma prioridade padrão com base na categoria do problema.
     * O usuário pode alterar essa sugestão antes/depois de salvar o chamado.
     */
    public Prioridade sugerirPrioridade(CategoriaProblema categoria) {
        if (categoria == null) {
            return Prioridade.MEDIA;
        }
        return switch (categoria) {
            case REDE -> Prioridade.CRITICA;
            case SOFTWARE -> Prioridade.ALTA;
            case HARDWARE -> Prioridade.MEDIA;
            case IMPRESSORA -> Prioridade.BAIXA;
            case OUTROS -> Prioridade.MEDIA;
        };
    }

    /**
     * Calcula e preenche os prazos de primeira resposta e resolução do
     * chamado, com base na prioridade atual e na configuração de SLA.
     */
    public void calcularPrazos(Chamado chamado) {
        Prioridade prioridade = chamado.getPrioridade() != null ? chamado.getPrioridade() : Prioridade.MEDIA;
        SlaConfig config = obterConfiguracao(prioridade);

        LocalDateTime base = chamado.getDataCriacao() != null ? chamado.getDataCriacao() : LocalDateTime.now();

        chamado.setPrazoPrimeiraResposta(base.plusMinutes(config.getTempoRespostaMinutos()));
        chamado.setPrazoResolucao(base.plusMinutes(config.getTempoResolucaoMinutos()));

        if (chamado.getStatusSla() == null) {
            chamado.setStatusSla(StatusSLA.NO_PRAZO);
        }
    }

    /**
     * Calcula a situação atual do SLA do chamado, considerando o status
     * do chamado, o prazo de resolução e o tempo restante.
     */
    public StatusSLA calcularStatusSla(Chamado chamado) {
        if (chamado.getPrazoResolucao() == null) {
            return StatusSLA.NO_PRAZO;
        }

        boolean finalizado = chamado.getStatus() == StatusChamado.RESOLVIDO
                || chamado.getStatus() == StatusChamado.FECHADO;

        if (finalizado) {
            LocalDateTime referencia = chamado.getUltimaAtualizacao() != null
                    ? chamado.getUltimaAtualizacao()
                    : LocalDateTime.now();
            return referencia.isAfter(chamado.getPrazoResolucao()) ? StatusSLA.ESTOURADO : StatusSLA.CUMPRIDO;
        }

        LocalDateTime agora = LocalDateTime.now();

        if (agora.isAfter(chamado.getPrazoResolucao())) {
            return StatusSLA.ESTOURADO;
        }

        if (chamado.getDataCriacao() != null) {
            long totalMinutos = Duration.between(chamado.getDataCriacao(), chamado.getPrazoResolucao()).toMinutes();
            long restanteMinutos = Duration.between(agora, chamado.getPrazoResolucao()).toMinutes();

            if (totalMinutos > 0 && restanteMinutos <= Math.ceil(totalMinutos * 0.2)) {
                return StatusSLA.EM_RISCO;
            }
        }

        return StatusSLA.NO_PRAZO;
    }

    /**
     * Lista a configuração de SLA para todas as prioridades, criando
     * valores padrão para as que ainda não foram configuradas.
     */
    public List<SlaConfigDTO> listarConfiguracoes() {
        List<SlaConfigDTO> resultado = new ArrayList<>();
        for (Prioridade prioridade : Prioridade.values()) {
            resultado.add(toDTO(obterConfiguracao(prioridade)));
        }
        return resultado;
    }

    /**
     * Atualiza (ou cria) a configuração de SLA de uma prioridade específica.
     */
    public SlaConfigDTO atualizarConfiguracao(Prioridade prioridade, SlaConfigDTO dto) {
        SlaConfig config = slaConfigRepository.findByPrioridade(prioridade)
                .orElseGet(() -> {
                    SlaConfig novo = new SlaConfig();
                    novo.setPrioridade(prioridade);
                    return novo;
                });

        if (dto.getTempoRespostaMinutos() != null && dto.getTempoRespostaMinutos() > 0) {
            config.setTempoRespostaMinutos(dto.getTempoRespostaMinutos());
        } else if (config.getTempoRespostaMinutos() == null) {
            config.setTempoRespostaMinutos(configPadrao(prioridade).getTempoRespostaMinutos());
        }

        if (dto.getTempoResolucaoMinutos() != null && dto.getTempoResolucaoMinutos() > 0) {
            config.setTempoResolucaoMinutos(dto.getTempoResolucaoMinutos());
        } else if (config.getTempoResolucaoMinutos() == null) {
            config.setTempoResolucaoMinutos(configPadrao(prioridade).getTempoResolucaoMinutos());
        }

        return toDTO(slaConfigRepository.save(config));
    }

    private SlaConfig obterConfiguracao(Prioridade prioridade) {
        return slaConfigRepository.findByPrioridade(prioridade)
                .orElseGet(() -> slaConfigRepository.save(configPadrao(prioridade)));
    }

    /**
     * Valores padrão de SLA, usados na primeira execução do sistema
     * (também recriados pelo DataInitializer).
     */
    private SlaConfig configPadrao(Prioridade prioridade) {
        SlaConfig config = new SlaConfig();
        config.setPrioridade(prioridade);
        switch (prioridade) {
            case CRITICA -> {
                config.setTempoRespostaMinutos(15);
                config.setTempoResolucaoMinutos(240); // 4 horas
            }
            case ALTA -> {
                config.setTempoRespostaMinutos(30);
                config.setTempoResolucaoMinutos(480); // 8 horas
            }
            case MEDIA -> {
                config.setTempoRespostaMinutos(60);
                config.setTempoResolucaoMinutos(1440); // 24 horas
            }
            case BAIXA -> {
                config.setTempoRespostaMinutos(120);
                config.setTempoResolucaoMinutos(2880); // 48 horas
            }
        }
        return config;
    }

    private SlaConfigDTO toDTO(SlaConfig config) {
        SlaConfigDTO dto = new SlaConfigDTO();
        dto.setId(config.getId());
        dto.setPrioridade(config.getPrioridade());
        dto.setTempoRespostaMinutos(config.getTempoRespostaMinutos());
        dto.setTempoResolucaoMinutos(config.getTempoResolucaoMinutos());
        return dto;
    }
}
