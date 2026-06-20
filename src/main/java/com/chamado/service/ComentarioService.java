package com.chamado.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.chamado.dto.ComentarioDTO;
import com.chamado.model.Chamado;
import com.chamado.model.Comentario;
import com.chamado.model.Historico;
import com.chamado.model.Usuario;
import com.chamado.model.enums.TipoEventoHistorico;
import com.chamado.repository.ChamadoRepository;
import com.chamado.repository.ComentarioRepository;
import com.chamado.repository.HistoricoRepository;
import com.chamado.repository.UsuarioRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoRepository historicoRepository;
    private final NotificacaoService notificacaoService;

    public ComentarioService(ComentarioRepository comentarioRepository, ChamadoRepository chamadoRepository,
            UsuarioRepository usuarioRepository, HistoricoRepository historicoRepository,
            NotificacaoService notificacaoService) {
        this.comentarioRepository = comentarioRepository;
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoRepository = historicoRepository;
        this.notificacaoService = notificacaoService;
    }

    public List<ComentarioDTO> listarPorChamado(Long chamadoId) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));

        return comentarioRepository.findByChamado(chamado)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public ComentarioDTO criarComentario(Long chamadoId, ComentarioDTO dto) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));

        // Buscar o usuário autor pelo id ou email fornecido no DTO
        Usuario autor = usuarioRepository.findByEmail(dto.getAutorNome())
                .orElseThrow(() -> new RuntimeException("Usuário autor não encontrado"));

        Comentario comentario = new Comentario();
        comentario.setTexto(dto.getTexto());
        comentario.setChamado(chamado);
        comentario.setAutor(autor);

        Comentario salvo = comentarioRepository.save(comentario);

        registrarHistorico(chamado, autor, salvo.getTexto());
        notificarNovoComentario(chamado, autor, salvo.getTexto());

        return toDTO(salvo);
    }

    public void excluirComentario(Long id) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));
        comentarioRepository.delete(comentario);
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
        }
        return dto;
    }
}