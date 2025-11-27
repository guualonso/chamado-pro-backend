package com.chamado.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.chamado.dto.ComentarioDTO;
import com.chamado.model.Chamado;
import com.chamado.model.Comentario;
import com.chamado.model.Usuario;
import com.chamado.repository.ChamadoRepository;
import com.chamado.repository.ComentarioRepository;
import com.chamado.repository.UsuarioRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository, ChamadoRepository chamadoRepository, UsuarioRepository usuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
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

        Usuario autor = usuarioRepository.findByEmail(dto.getAutorNome())
                .orElseThrow(() -> new RuntimeException("Usuário autor não encontrado"));

        Comentario comentario = new Comentario();
        comentario.setTexto(dto.getTexto());
        comentario.setChamado(chamado);
        comentario.setAutor(autor);

        Comentario salvo = comentarioRepository.save(comentario);
        return toDTO(salvo);
    }

    public void excluirComentario(Long id) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));
        comentarioRepository.delete(comentario);
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
