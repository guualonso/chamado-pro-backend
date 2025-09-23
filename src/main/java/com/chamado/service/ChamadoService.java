package com.chamado.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.chamado.model.Chamado;
import com.chamado.model.Usuario;
import com.chamado.model.enums.StatusChamado;
import com.chamado.repository.ChamadoRepository;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;

    public ChamadoService(ChamadoRepository chamadoRepository) {
        this.chamadoRepository = chamadoRepository;
    }

    public List<Chamado> listarTodos() {
        return chamadoRepository.findAll();
    }

    public List<Chamado> listarPorCliente(Usuario cliente) {
        return chamadoRepository.findByCliente(cliente);
    }

    public List<Chamado> listarPorTecnico(Usuario tecnico) {
        return chamadoRepository.findByTecnico(tecnico);
    }

    public List<Chamado> listarPorAdmin(Usuario admin) {
        return chamadoRepository.findByAdmin(admin);
    }

    public Chamado criarChamado(Chamado chamado) {
        chamado.setDataCriacao(LocalDateTime.now());
        chamado.setStatus(StatusChamado.ABERTO);
        return chamadoRepository.save(chamado);
    }

    public Chamado atualizarChamado(Long id, Chamado chamadoAtualizado) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));

        chamado.setTitulo(chamadoAtualizado.getTitulo());
        chamado.setDescricao(chamadoAtualizado.getDescricao());
        chamado.setStatus(chamadoAtualizado.getStatus());
        chamado.setUltimaAtualizacao(LocalDateTime.now());

        return chamadoRepository.save(chamado);
    }

    public Chamado atribuirTecnico(Long id, Usuario tecnico) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        chamado.setTecnico(tecnico);
        chamado.setUltimaAtualizacao(LocalDateTime.now());
        return chamadoRepository.save(chamado);
    }
}