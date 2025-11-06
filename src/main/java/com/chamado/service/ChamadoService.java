package com.chamado.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chamado.dto.ChamadoDTO;
import com.chamado.model.Chamado;
import com.chamado.model.Usuario;
// import com.chamado.model.enums.CategoriaProblema;
import com.chamado.model.enums.StatusChamado;
import com.chamado.repository.ChamadoRepository;
import com.chamado.repository.UsuarioRepository;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public ChamadoService(ChamadoRepository chamadoRepository, UsuarioRepository usuarioRepository) {
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ChamadoDTO criarChamado(ChamadoDTO dto) {
        Chamado chamado = new Chamado();
        chamado.setTitulo(dto.getTitulo());
        chamado.setDescricao(dto.getDescricao());
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataCriacao(LocalDateTime.now());

        if (dto.getCategoria() != null) {
            chamado.setCategoria(dto.getCategoria());
        }

        if (dto.getClienteId() != null) {
            var cliente = usuarioRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            chamado.setCliente(cliente);
        }

        Chamado salvo = chamadoRepository.save(chamado);
        return toDTO(salvo);
    }

    public List<ChamadoDTO> listarTodos() {
        return chamadoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ChamadoDTO buscarPorId(Long id) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        return toDTO(chamado);
    }

    public ChamadoDTO atualizarChamado(Long id, ChamadoDTO dto) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));

        chamado.setTitulo(dto.getTitulo());
        chamado.setDescricao(dto.getDescricao());
        if (dto.getStatus() != null)
            chamado.setStatus(dto.getStatus());

        if (dto.getCategoria() != null)
            chamado.setCategoria(dto.getCategoria());

        chamado.setUltimaAtualizacao(LocalDateTime.now());

        Chamado atualizado = chamadoRepository.save(chamado);
        return toDTO(atualizado);
    }

    public void excluirChamado(Long id) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        chamadoRepository.delete(chamado);
    }

   public List<ChamadoDTO> listarPorCliente(Long clienteId) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return chamadoRepository.findByCliente(cliente)
                .stream().map(this::toDTO).toList();
    }

    public List<ChamadoDTO> listarPorTecnico(Long tecnicoId) {
        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado"));
        return chamadoRepository.findByTecnico(tecnico)
                .stream().map(this::toDTO).toList();
    }

    public List<ChamadoDTO> listarPorAdmin(Long adminId) {
        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin não encontrado"));
        return chamadoRepository.findByAdmin(admin)
                .stream().map(this::toDTO).toList();
    }

    public List<ChamadoDTO> listarPendentes() {
        return chamadoRepository.findByStatus(StatusChamado.ABERTO)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public ChamadoDTO atribuirTecnico(Long chamadoId, Long tecnicoId) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado"));

        chamado.setTecnico(tecnico);
        chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        chamado.setUltimaAtualizacao(LocalDateTime.now());

        return toDTO(chamadoRepository.save(chamado));
    }

    private ChamadoDTO toDTO(Chamado chamado) {
        ChamadoDTO dto = new ChamadoDTO();
        dto.setId(chamado.getId());
        dto.setTitulo(chamado.getTitulo());
        dto.setDescricao(chamado.getDescricao());
        dto.setStatus(chamado.getStatus());
        dto.setDataCriacao(chamado.getDataCriacao());
        dto.setUltimaAtualizacao(chamado.getUltimaAtualizacao());
        dto.setCategoria(chamado.getCategoria());
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