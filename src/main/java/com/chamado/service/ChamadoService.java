package com.chamado.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.chamado.model.Chamado;
import com.chamado.model.Usuario;
// import com.chamado.model.enums.CategoriaProblema; // Implementação futura
import com.chamado.model.enums.StatusChamado;
import com.chamado.repository.ChamadoRepository;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;

    public ChamadoService(ChamadoRepository chamadoRepository) {
        this.chamadoRepository = chamadoRepository;
    }

    public Chamado criarChamado(Chamado chamado) {
        // Validações de negócio
        if (chamado.getCliente() == null) {
            throw new RuntimeException("Cliente é obrigatório para abrir um chamado");
        }
        
        if (chamado.getTitulo() == null || chamado.getTitulo().trim().isEmpty()) {
            throw new RuntimeException("Título é obrigatório");
        }
        
        // Define valores padrão
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataCriacao(LocalDateTime.now());
        
        return chamadoRepository.save(chamado);
    }

    public List<Chamado> listarTodos() {
        return chamadoRepository.findAll();
    }

    public List<Chamado> listarPorCliente(Usuario cliente) {
        return chamadoRepository.findByCliente(cliente);
    }

    public Chamado buscarPorId(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
    }

    public Chamado atualizarChamado(Long id, Chamado chamadoAtualizado) {
        Chamado chamado = buscarPorId(id);

        // Lógica de negócio para atualização
        chamado.setTitulo(chamadoAtualizado.getTitulo());
        chamado.setDescricao(chamadoAtualizado.getDescricao());
        chamado.setCategoria(chamadoAtualizado.getCategoria());
        
        // O status só é atualizado se for fornecido
        if (chamadoAtualizado.getStatus() != null) {
            chamado.setStatus(chamadoAtualizado.getStatus());
        }

        return chamadoRepository.save(chamado);
    }

    public void excluirChamado(Long id) {
        Chamado chamado = buscarPorId(id);
        chamadoRepository.delete(chamado);
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