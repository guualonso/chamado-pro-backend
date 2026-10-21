package com.chamado.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.chamado.dto.ComentarioDTO;
import com.chamado.exception.RecursoNaoEncontradoException;
import com.chamado.model.Chamado;
import com.chamado.model.Comentario;
import com.chamado.model.Historico;
import com.chamado.model.Usuario;
import com.chamado.model.enums.TipoEventoHistorico;
import com.chamado.repository.ChamadoRepository;
import com.chamado.repository.ComentarioRepository;
import com.chamado.repository.HistoricoRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;
    private final HistoricoRepository historicoRepository;
    private final NotificacaoService notificacaoService;

    public ComentarioService(ComentarioRepository comentarioRepository, ChamadoRepository chamadoRepository,
            HistoricoRepository historicoRepository, NotificacaoService notificacaoService) {
        this.comentarioRepository = comentarioRepository;
        this.chamadoRepository = chamadoRepository;
        this.historicoRepository = historicoRepository;
        this.notificacaoService = notificacaoService;
    }

    public List<ComentarioDTO> listarPorChamado(Long chamadoId, Usuario solicitante) {
        Chamado chamado = buscarChamado(chamadoId);
        CurrentUserService.verificarAcessoChamado(solicitante, chamado);

        return comentarioRepository.findByChamado(chamado)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * O autor é sempre o usuário autenticado; não é aceito do corpo da
     * requisição, evitando que alguém comente em nome de outra pessoa.
     */
    public ComentarioDTO criarComentario(Long chamadoId, ComentarioDTO dto, Usuario autor) {
        Chamado chamado = buscarChamado(chamadoId);
        CurrentUserService.verificarAcessoChamado(autor, chamado);

        Comentario comentario = new Comentario();
        comentario.setTexto(dto.getTexto());
        comentario.setChamado(chamado);
        comentario.setAutor(autor);

        Comentario salvo = comentarioRepository.save(comentario);

        registrarHistorico(chamado, autor, salvo.getTexto());
        notificarNovoComentario(chamado, autor, salvo.getTexto());

        return toDTO(salvo);
    }

    /** Somente o autor do comentário ou um ADMIN pode excluí-lo. */
    public void excluirComentario(Long id, Usuario solicitante) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comentário não encontrado"));
        boolean autor = comentario.getAutor() != null && comentario.getAutor().getId().equals(solicitante.getId());
        if (!autor && !CurrentUserService.isAdmin(solicitante)) {
            throw new AccessDeniedException("Somente o autor ou um administrador pode excluir o comentário");
        }
        comentarioRepository.delete(comentario);
    }

    private Chamado buscarChamado(Long chamadoId) {
        return chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado"));
    }

    private void registrarHistorico(Chamado chamado, Usuario autor, String texto) {
        Historico historico = new Historico();
        historico.setChamado(chamado);
        historico.setUsuario(autor);
        historico.setTipoEvento(TipoEventoHistorico.COMENTARIO);
        historico.setDescricao(autor.getNome() + " comentou: " + resumir(texto));
        historicoRepository.save(historico);
    }

    /**
     * Notifica o "outro lado" do chamado sobre o novo comentário:
     * se quem comentou foi o cliente, avisa o técnico responsável (e vice-versa).
     */
    private void notificarNovoComentario(Chamado chamado, Usuario autor, String texto) {
        String mensagem = autor.getNome() + " comentou no chamado #" + chamado.getId()
                + " (" + chamado.getTitulo() + "): " + resumir(texto);

        Usuario cliente = chamado.getCliente();
        Usuario tecnico = chamado.getTecnico();

        boolean autorEhCliente = cliente != null && cliente.getId().equals(autor.getId());

        if (autorEhCliente) {
            if (tecnico != null) {
                notificacaoService.notificar(tecnico, chamado, mensagem);
            }
        } else {
            if (cliente != null && !cliente.getId().equals(autor.getId())) {
                notificacaoService.notificar(cliente, chamado, mensagem);
            }
        }
    }

    private String resumir(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.length() > 120 ? texto.substring(0, 117) + "..." : texto;
    }

    private ComentarioDTO toDTO(Comentario comentario) {
        ComentarioDTO dto = new ComentarioDTO();
        dto.setId(comentario.getId());
        dto.setTexto(comentario.getTexto());
        dto.setDataCriacao(comentario.getDataCriacao());
        dto.setChamadoId(comentario.getChamado().getId());
        if (comentario.getAutor() != null) {
            dto.setAutorNome(comentario.getAutor().getNome());
            dto.setAutorId(comentario.getAutor().getId());
        }
        return dto;
    }
}