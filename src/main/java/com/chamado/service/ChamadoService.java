package com.chamado.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.chamado.dto.ChamadoDTO;
import com.chamado.model.Chamado;
// import com.chamado.model.Usuario; // Implementação futura
// import com.chamado.model.enums.CategoriaProblema; // Implementação futura
import com.chamado.model.enums.StatusChamado;
import com.chamado.repository.ChamadoRepository;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;

    public ChamadoService(ChamadoRepository chamadoRepository) {
        this.chamadoRepository = chamadoRepository;
    }

    public ChamadoDTO criarChamado(ChamadoDTO dto) {
        Chamado chamado = new Chamado();
        chamado.setTitulo(dto.getTitulo());
        chamado.setDescricao(dto.getDescricao());
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataCriacao(LocalDateTime.now());

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
        chamado.setStatus(dto.getStatus() != null ? dto.getStatus() : chamado.getStatus());
        chamado.setUltimaAtualizacao(LocalDateTime.now());

        Chamado atualizado = chamadoRepository.save(chamado);
        return toDTO(atualizado);
    }

    public void excluirChamado(Long id) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        chamadoRepository.delete(chamado);
    }

    private ChamadoDTO toDTO(Chamado chamado) {
        ChamadoDTO dto = new ChamadoDTO();
        dto.setId(chamado.getId());
        dto.setTitulo(chamado.getTitulo());
        dto.setDescricao(chamado.getDescricao());
        dto.setStatus(chamado.getStatus());
        dto.setDataCriacao(chamado.getDataCriacao());
        dto.setUltimaAtualizacao(chamado.getUltimaAtualizacao());

        if (chamado.getCliente() != null) {
            dto.setClienteNome(chamado.getCliente().getNome());
        }
        /**if (chamado.getTecnico() != null) {
            dto.setTecnicoNome(chamado.getTecnico().getNome());
        }
        if (chamado.getAdmin() != null) {
            dto.setAdminNome(chamado.getAdmin().getNome());
        }**/

        return dto;
    }

    /*
    public List<Chamado> listarPorTecnico(Usuario tecnico) {
        return chamadoRepository.findByTecnico(tecnico);
    }

    public Chamado atribuirTecnico(Long id, Usuario tecnico) {
        Chamado chamado = buscarPorId(id);
        chamado.setTecnico(tecnico);
        chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        return chamadoRepository.save(chamado);
    }

    public List<Chamado> listarPorAdmin(Usuario admin) {
        return chamadoRepository.findByAdmin(admin);
    }
    */
}