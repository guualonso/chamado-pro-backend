package com.chamado.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chamado.dto.EscalonarRequestDTO;
import com.chamado.model.Chamado;
import com.chamado.model.Historico;
import com.chamado.model.Usuario;
import com.chamado.model.enums.NivelTecnico;
import com.chamado.model.enums.StatusChamado;
import com.chamado.model.enums.TipoEventoHistorico;
import com.chamado.model.enums.TipoUsuario;
import com.chamado.repository.ChamadoRepository;
import com.chamado.repository.HistoricoRepository;
import com.chamado.repository.UsuarioRepository;

/**
 * Responsável pelo escalonamento de chamados entre os níveis N1, N2 e N3,
 * tanto de forma manual (acionado por um técnico/admin) quanto automática
 * (acionado pelo {@link SlaSchedulerService} ao detectar estouro de SLA).
 */
@Service
public class EscalonamentoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoRepository historicoRepository;
    private final NotificacaoService notificacaoService;

    public EscalonamentoService(ChamadoRepository chamadoRepository,
                                 UsuarioRepository usuarioRepository,
                                 HistoricoRepository historicoRepository,
                                 NotificacaoService notificacaoService) {
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoRepository = historicoRepository;
        this.notificacaoService = notificacaoService;
    }

    /**
     * Escalonamento manual, acionado por um técnico ou administrador.
     */
    @Transactional
    public Chamado escalonarManual(Long chamadoId, EscalonarRequestDTO request, Usuario solicitante) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));

        NivelTecnico nivelAtual = chamado.getNivelAtual() != null ? chamado.getNivelAtual() : NivelTecnico.N1;
        NivelTecnico novoNivel = request.getNovoNivel() != null ? request.getNovoNivel() : proximoNivel(nivelAtual);

        if (novoNivel.ordinal() <= nivelAtual.ordinal()) {
            throw new RuntimeException("O novo nível precisa ser maior que o nível atual do chamado");
        }

        return aplicarEscalonamento(chamado, novoNivel, request.getTecnicoId(), request.getMotivo(), solicitante, false);
    }

    /**
     * Escalonamento automático, disparado quando o SLA de um chamado estoura.
     * Não faz nada se o chamado já estiver no nível máximo (N3).
     */
    @Transactional
    public void escalonarAutomatico(Chamado chamado) {
        NivelTecnico nivelAtual = chamado.getNivelAtual() != null ? chamado.getNivelAtual() : NivelTecnico.N1;
        if (nivelAtual == NivelTecnico.N3) {
            return;
        }
        NivelTecnico novoNivel = proximoNivel(nivelAtual);
        aplicarEscalonamento(chamado, novoNivel, null, "Escalonamento automático por estouro de SLA", null, true);
    }

    private Chamado aplicarEscalonamento(Chamado chamado, NivelTecnico novoNivel, Long tecnicoId,
                                          String motivo, Usuario solicitante, boolean automatico) {
        NivelTecnico nivelAnterior = chamado.getNivelAtual() != null ? chamado.getNivelAtual() : NivelTecnico.N1;
        chamado.setNivelAtual(novoNivel);

        Usuario novoTecnico = null;
        if (tecnicoId != null) {
            novoTecnico = usuarioRepository.findById(tecnicoId)
                    .orElseThrow(() -> new RuntimeException("Técnico não encontrado"));
            if (novoTecnico.getTipoUsuario() != TipoUsuario.TECNICO) {
                throw new RuntimeException("O usuário informado não é um técnico");
            }
        } else {
            List<Usuario> candidatos = usuarioRepository.findByTipoUsuarioAndNivelTecnico(TipoUsuario.TECNICO, novoNivel);
            if (!candidatos.isEmpty()) {
                novoTecnico = candidatos.get(0);
            }
        }

        if (novoTecnico != null) {
            chamado.setTecnico(novoTecnico);
            if (chamado.getDataPrimeiraResposta() == null) {
                chamado.setDataPrimeiraResposta(LocalDateTime.now());
            }
        }

        if (chamado.getStatus() == StatusChamado.ABERTO) {
            chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        }
        chamado.setUltimaAtualizacao(LocalDateTime.now());

        Chamado salvo = chamadoRepository.save(chamado);

        StringBuilder descricao = new StringBuilder();
        descricao.append(automatico ? "Escalonamento automático" : "Escalonamento manual")
                .append(" de ").append(nivelAnterior).append(" para ").append(novoNivel);
        if (novoTecnico != null) {
            descricao.append(" - atribuído a ").append(novoTecnico.getNome())
                    .append(" (").append(novoNivel).append(")");
        } else {
            descricao.append(" - nenhum técnico disponível nesse nível, chamado permanece na fila");
        }
        if (motivo != null && !motivo.isBlank()) {
            descricao.append(". Motivo: ").append(motivo);
        }

        Historico historico = new Historico();
        historico.setChamado(salvo);
        historico.setUsuario(solicitante);
        historico.setTipoEvento(TipoEventoHistorico.ESCALONAMENTO);
        historico.setDescricao(descricao.toString());
        historicoRepository.save(historico);

        if (novoTecnico != null) {
            notificacaoService.notificar(novoTecnico, salvo,
                    "Chamado #" + salvo.getId() + " (" + salvo.getTitulo() + ") foi escalonado para "
                            + novoNivel + " e atribuído a você.");
        }

        if (salvo.getCliente() != null) {
            notificacaoService.notificar(salvo.getCliente(), salvo,
                    "Seu chamado #" + salvo.getId() + " (" + salvo.getTitulo() + ") foi escalonado para o nível "
                            + novoNivel + ".");
        }

        return salvo;
    }

    private NivelTecnico proximoNivel(NivelTecnico atual) {
        return switch (atual) {
            case N1 -> NivelTecnico.N2;
            case N2 -> NivelTecnico.N3;
            case N3 -> NivelTecnico.N3;
        };
    }
}
