package com.chamado.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.chamado.dto.ChamadoDTO;
import com.chamado.dto.EscalonarRequestDTO;
import com.chamado.dto.HistoricoDTO;
import com.chamado.exception.RecursoNaoEncontradoException;
import com.chamado.exception.RegraNegocioException;
import com.chamado.model.Chamado;
import com.chamado.model.Historico;
import com.chamado.model.Usuario;
import com.chamado.model.enums.NivelTecnico;
import com.chamado.model.enums.Prioridade;
import com.chamado.model.enums.StatusChamado;
import com.chamado.model.enums.TipoEventoHistorico;
import com.chamado.model.enums.TipoUsuario;
import com.chamado.repository.ChamadoRepository;
import com.chamado.repository.HistoricoRepository;
import com.chamado.repository.UsuarioRepository;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SlaService slaService;
    private final NotificacaoService notificacaoService;
    private final HistoricoRepository historicoRepository;
    private final EscalonamentoService escalonamentoService;

    public ChamadoService(ChamadoRepository chamadoRepository,
                           UsuarioRepository usuarioRepository,
                           SlaService slaService,
                           NotificacaoService notificacaoService,
                           HistoricoRepository historicoRepository,
                           EscalonamentoService escalonamentoService) {
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.slaService = slaService;
        this.notificacaoService = notificacaoService;
        this.historicoRepository = historicoRepository;
        this.escalonamentoService = escalonamentoService;
    }

    public ChamadoDTO criarChamado(ChamadoDTO dto, Usuario solicitante) {
        if (dto.getTitulo() == null || dto.getTitulo().isBlank()
                || dto.getDescricao() == null || dto.getDescricao().isBlank()) {
            throw new RegraNegocioException("Título e descrição são obrigatórios");
        }

        Chamado chamado = new Chamado();
        chamado.setTitulo(dto.getTitulo());
        chamado.setDescricao(dto.getDescricao());
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataCriacao(LocalDateTime.now());
        chamado.setNivelAtual(NivelTecnico.N1);

        if (dto.getCategoria() != null) {
            chamado.setCategoria(dto.getCategoria());
        }

        Prioridade prioridade = dto.getPrioridade() != null
                ? dto.getPrioridade()
                : slaService.sugerirPrioridade(dto.getCategoria());
        chamado.setPrioridade(prioridade);

        // O cliente do chamado é sempre o usuário autenticado. Somente um ADMIN
        // pode abrir um chamado em nome de outro usuário (clienteId).
        Usuario cliente = solicitante;
        if (CurrentUserService.isAdmin(solicitante) && dto.getClienteId() != null) {
            cliente = usuarioRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
        }
        chamado.setCliente(cliente);

        slaService.calcularPrazos(chamado);

        Chamado salvo = chamadoRepository.save(chamado);

        registrarHistorico(salvo, solicitante, TipoEventoHistorico.CRIACAO,
                "Chamado criado com prioridade " + prioridade + " (nível " + salvo.getNivelAtual() + ")");

        notificarNovoChamado(salvo);

        return toDTO(salvo);
    }

    public List<ChamadoDTO> listarTodos() {
        return chamadoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ChamadoDTO buscarPorId(Long id, Usuario solicitante) {
        Chamado chamado = buscarEntidade(id);
        CurrentUserService.verificarAcessoChamado(solicitante, chamado);
        return toDTO(chamado);
    }

    /**
     * Atualiza o chamado respeitando o perfil de quem solicita:
     * - CLIENTE (dono): pode editar título/descrição e enviar avaliação/feedback;
     * - TECNICO/ADMIN: podem alterar status, categoria e prioridade.
     */
    public ChamadoDTO atualizarChamado(Long id, ChamadoDTO dto, Usuario solicitante) {
        Chamado chamado = buscarEntidade(id);
        CurrentUserService.verificarAcessoChamado(solicitante, chamado);

        boolean equipe = CurrentUserService.isAdmin(solicitante) || CurrentUserService.isTecnico(solicitante);
        boolean dono = chamado.getCliente() != null && chamado.getCliente().getId().equals(solicitante.getId());

        if (dto.getTitulo() != null && !dto.getTitulo().isBlank()) {
            chamado.setTitulo(dto.getTitulo());
        }
        if (dto.getDescricao() != null && !dto.getDescricao().isBlank()) {
            chamado.setDescricao(dto.getDescricao());
        }

        StatusChamado statusAnterior = chamado.getStatus();
        if (equipe && dto.getStatus() != null && dto.getStatus() != statusAnterior) {
            chamado.setStatus(dto.getStatus());

            if (chamado.getDataPrimeiraResposta() == null && dto.getStatus() == StatusChamado.EM_ANDAMENTO) {
                chamado.setDataPrimeiraResposta(LocalDateTime.now());
            }

            registrarHistorico(chamado, solicitante, TipoEventoHistorico.MUDANCA_STATUS,
                    "Status alterado de " + statusAnterior + " para " + dto.getStatus());
        } else if (!equipe && dto.getStatus() != null && dto.getStatus() != statusAnterior) {
            throw new AccessDeniedException("Somente técnicos e administradores podem alterar o status");
        }

        if (equipe && dto.getCategoria() != null) {
            chamado.setCategoria(dto.getCategoria());
        }

        Prioridade prioridadeAnterior = chamado.getPrioridade();
        if (equipe && dto.getPrioridade() != null && dto.getPrioridade() != prioridadeAnterior) {
            chamado.setPrioridade(dto.getPrioridade());
            slaService.calcularPrazos(chamado);
            registrarHistorico(chamado, solicitante, TipoEventoHistorico.MUDANCA_PRIORIDADE,
                    "Prioridade alterada de " + prioridadeAnterior + " para " + dto.getPrioridade());
        }

        // Avaliação e feedback são exclusivos do cliente que abriu o chamado
        if (dono) {
            if (dto.getAvaliacao() > 0) {
                chamado.setAvaliacao(dto.getAvaliacao());
            }
            if (dto.getFeedback() != null && !dto.getFeedback().isBlank()) {
                chamado.setFeedback(dto.getFeedback());
            }
        }

        chamado.setUltimaAtualizacao(LocalDateTime.now());
        chamado.setStatusSla(slaService.calcularStatusSla(chamado));

        Chamado atualizado = chamadoRepository.save(chamado);

        if (atualizado.getStatus() != statusAnterior && atualizado.getCliente() != null) {
            notificacaoService.notificar(atualizado.getCliente(), atualizado,
                    "O status do seu chamado #" + atualizado.getId() + " (" + atualizado.getTitulo()
                            + ") mudou para " + atualizado.getStatus() + ".");
        }

        return toDTO(atualizado);
    }

    public void excluirChamado(Long id) {
        chamadoRepository.delete(buscarEntidade(id));
    }

    public List<ChamadoDTO> listarPorCliente(Long clienteId, Usuario solicitante) {
        if (!CurrentUserService.isAdmin(solicitante) && !solicitante.getId().equals(clienteId)) {
            throw new AccessDeniedException("Acesso negado aos chamados de outro cliente");
        }
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
        return chamadoRepository.findByCliente(cliente)
                .stream().map(this::toDTO).toList();
    }

    public List<ChamadoDTO> listarPorTecnico(Long tecnicoId, Usuario solicitante) {
        if (!CurrentUserService.isAdmin(solicitante) && !solicitante.getId().equals(tecnicoId)) {
            throw new AccessDeniedException("Acesso negado aos chamados de outro técnico");
        }
        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico não encontrado"));
        return chamadoRepository.findByTecnico(tecnico)
                .stream().map(this::toDTO).toList();
    }

    public List<ChamadoDTO> listarPorAdmin(Long adminId) {
        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Admin não encontrado"));
        return chamadoRepository.findByAdmin(admin)
                .stream().map(this::toDTO).toList();
    }

    /**
     * ADMIN pode atribuir qualquer técnico; um TECNICO só pode atribuir a si mesmo.
     */
    public ChamadoDTO atribuirTecnico(Long chamadoId, Long tecnicoId, Usuario solicitante) {
        if (!CurrentUserService.isAdmin(solicitante) && !solicitante.getId().equals(tecnicoId)) {
            throw new AccessDeniedException("Técnicos só podem atribuir chamados a si mesmos");
        }
        Chamado chamado = buscarEntidade(chamadoId);
        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico não encontrado"));
        if (tecnico.getTipoUsuario() != TipoUsuario.TECNICO) {
            throw new RegraNegocioException("O usuário informado não é um técnico");
        }

        chamado.setTecnico(tecnico);
        chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        if (chamado.getDataPrimeiraResposta() == null) {
            chamado.setDataPrimeiraResposta(LocalDateTime.now());
        }
        chamado.setUltimaAtualizacao(LocalDateTime.now());
        chamado.setStatusSla(slaService.calcularStatusSla(chamado));

        Chamado salvo = chamadoRepository.save(chamado);

        String nivelTecnico = tecnico.getNivelTecnico() != null ? tecnico.getNivelTecnico().toString() : "N1";
        registrarHistorico(salvo, tecnico, TipoEventoHistorico.ATRIBUICAO,
                "Chamado atribuído ao técnico " + tecnico.getNome() + " (" + nivelTecnico + ")");

        notificacaoService.notificar(tecnico, salvo,
                "O chamado #" + salvo.getId() + " (" + salvo.getTitulo() + ") foi atribuído a você.");

        return toDTO(salvo);
    }

    /**
     * Escalona manualmente o chamado para o próximo nível (ou para o nível
     * informado), opcionalmente atribuindo a um técnico específico.
     */
    public ChamadoDTO escalonarChamado(Long id, EscalonarRequestDTO request, Usuario solicitante) {
        Chamado chamado = escalonamentoService.escalonarManual(id, request, solicitante);
        return toDTO(chamado);
    }

    /**
     * Lista o histórico (auditoria) de eventos do chamado, mais recentes primeiro.
     */
    public List<HistoricoDTO> listarHistorico(Long chamadoId, Usuario solicitante) {
        Chamado chamado = buscarEntidade(chamadoId);
        CurrentUserService.verificarAcessoChamado(solicitante, chamado);
        return historicoRepository.findByChamadoOrderByDataHoraDesc(chamado)
                .stream()
                .map(this::toHistoricoDTO)
                .toList();
    }

    private Chamado buscarEntidade(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado"));
    }

    private void registrarHistorico(Chamado chamado, Usuario usuario, TipoEventoHistorico tipo, String descricao) {
        Historico historico = new Historico();
        historico.setChamado(chamado);
        historico.setUsuario(usuario);
        historico.setTipoEvento(tipo);
        historico.setDescricao(descricao);
        historicoRepository.save(historico);
    }

    private void notificarNovoChamado(Chamado chamado) {
        if (chamado.getCliente() != null) {
            notificacaoService.notificar(chamado.getCliente(), chamado,
                    "Seu chamado #" + chamado.getId() + " (" + chamado.getTitulo() + ") foi aberto com prioridade "
                            + chamado.getPrioridade() + ".");
        }

        usuarioRepository.findByTipoUsuarioAndNivelTecnico(TipoUsuario.TECNICO, NivelTecnico.N1)
                .forEach(tecnico -> notificacaoService.notificar(tecnico, chamado,
                        "Novo chamado #" + chamado.getId() + " (" + chamado.getTitulo() + ") aberto - prioridade "
                                + chamado.getPrioridade() + "."));
    }

    private HistoricoDTO toHistoricoDTO(Historico historico) {
        HistoricoDTO dto = new HistoricoDTO();
        dto.setId(historico.getId());
        dto.setChamadoId(historico.getChamado().getId());
        dto.setUsuarioNome(historico.getUsuario() != null ? historico.getUsuario().getNome() : "Sistema");
        dto.setTipoEvento(historico.getTipoEvento());
        dto.setDescricao(historico.getDescricao());
        dto.setDataHora(historico.getDataHora());
        return dto;
    }

    public ChamadoDTO toDTO(Chamado chamado) {
        ChamadoDTO dto = new ChamadoDTO();
        dto.setId(chamado.getId());
        dto.setTitulo(chamado.getTitulo());
        dto.setDescricao(chamado.getDescricao());
        dto.setStatus(chamado.getStatus());
        dto.setDataCriacao(chamado.getDataCriacao());
        dto.setUltimaAtualizacao(chamado.getUltimaAtualizacao());
        dto.setCategoria(chamado.getCategoria());
        dto.setPrioridade(chamado.getPrioridade());
        dto.setNivelAtual(chamado.getNivelAtual());
        dto.setDataPrimeiraResposta(chamado.getDataPrimeiraResposta());
        dto.setPrazoPrimeiraResposta(chamado.getPrazoPrimeiraResposta());
        dto.setPrazoResolucao(chamado.getPrazoResolucao());
        dto.setStatusSla(chamado.getStatusSla());
        dto.setAvaliacao(chamado.getAvaliacao());
        dto.setFeedback(chamado.getFeedback());

        if (chamado.getCliente() != null) {
            dto.setClienteId(chamado.getCliente().getId());
            dto.setClienteNome(chamado.getCliente().getNome());
        }
        if (chamado.getTecnico() != null) {
            dto.setTecnicoId(chamado.getTecnico().getId());
            dto.setTecnicoNome(chamado.getTecnico().getNome());
        }
        if (chamado.getAdmin() != null) {
            dto.setAdminId(chamado.getAdmin().getId());
            dto.setAdminNome(chamado.getAdmin().getNome());
        }
        return dto;
    }
}
