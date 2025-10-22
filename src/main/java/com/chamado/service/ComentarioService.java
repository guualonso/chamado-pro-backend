package com.chamado.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.chamado.dto.ComentarioDTO;
import com.chamado.model.Chamado;
import com.chamado.model.Comentario;
import com.chamado.repository.ChamadoRepository;
import com.chamado.repository.ComentarioRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;

    public ComentarioService(ComentarioRepository comentarioRepository, ChamadoRepository chamadoRepository) {
        this.comentarioRepository = comentarioRepository;
        this.chamadoRepository = chamadoRepository;
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

        Comentario comentario = new Comentario();
        comentario.setTexto(dto.getTexto());
        comentario.setChamado(chamado);

        // o usuário será implementado futuramente com autenticação
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
