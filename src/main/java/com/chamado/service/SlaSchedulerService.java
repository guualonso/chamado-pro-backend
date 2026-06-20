package com.chamado.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.chamado.model.Chamado;
import com.chamado.model.enums.StatusChamado;
import com.chamado.model.enums.StatusSLA;
import com.chamado.repository.ChamadoRepository;

/**
 * Job agendado responsável por monitorar o SLA dos chamados em aberto.
 * - Quando um chamado entra em risco (faltando ~20% do prazo), notifica o técnico responsável.
 * - Quando o prazo de resolução estoura, notifica o responsável e escalona
 *   automaticamente o chamado para o próximo nível (N1 -> N2 -> N3),
 *   se a opção estiver habilitada.
 */
@Component
public class SlaSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SlaSchedulerService.class);

    private final ChamadoRepository chamadoRepository;
    private final SlaService slaService;
    private final EscalonamentoService escalonamentoService;
    private final NotificacaoService notificacaoService;

    @Value("${sla.escalonamento-automatico:true}")
    private boolean escalonamentoAutomaticoHabilitado;

    public SlaSchedulerService(ChamadoRepository chamadoRepository,
                                SlaService slaService,
                                EscalonamentoService escalonamentoService,
                                NotificacaoService notificacaoService) {
        this.chamadoRepository = chamadoRepository;
        this.slaService = slaService;
        this.escalonamentoService = escalonamentoService;
        this.notificacaoService = notificacaoService;
    }

    @Scheduled(fixedRateString = "${sla.scheduler.intervalo-ms:60000}")
    @Transactional
    public void verificarSlas() {
        List<Chamado> emAberto = chamadoRepository.findByStatusIn(
                List.of(StatusChamado.ABERTO, StatusChamado.EM_ANDAMENTO));

        for (Chamado chamado : emAberto) {
            StatusSLA statusAnterior = chamado.getStatusSla();
            StatusSLA statusAtual = slaService.calcularStatusSla(chamado);

            if (statusAtual == statusAnterior) {
                continue;
            }

            chamado.setStatusSla(statusAtual);
            chamadoRepository.save(chamado);

            if (statusAtual == StatusSLA.EM_RISCO) {
                notificarSlaEmRisco(chamado);
            } else if (statusAtual == StatusSLA.ESTOURADO) {
                notificarSlaEstourado(chamado);
                if (escalonamentoAutomaticoHabilitado) {
                    log.info("SLA do chamado #{} estourou - escalonando automaticamente", chamado.getId());
                    escalonamentoService.escalonarAutomatico(chamado);
                }
            }
        }
    }

    private void notificarSlaEmRisco(Chamado chamado) {
        String mensagem = "Atenção: o SLA do chamado #" + chamado.getId() + " (" + chamado.getTitulo()
                + ") está em risco de estourar.";
        if (chamado.getTecnico() != null) {
            notificacaoService.notificar(chamado.getTecnico(), chamado, mensagem);
        }
    }

    private void notificarSlaEstourado(Chamado chamado) {
        String mensagem = "O SLA do chamado #" + chamado.getId() + " (" + chamado.getTitulo() + ") estourou.";
        if (chamado.getTecnico() != null) {
            notificacaoService.notificar(chamado.getTecnico(), chamado, mensagem);
        }
    }
}
