package com.chamado.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chamado.dto.ChamadoDTO;
import com.chamado.dto.EscalonarRequestDTO;
import com.chamado.dto.HistoricoDTO;
import com.chamado.model.Usuario;
import com.chamado.service.ChamadoService;
import com.chamado.service.CurrentUserService;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final CurrentUserService currentUserService;

    public ChamadoController(ChamadoService chamadoService, CurrentUserService currentUserService) {
        this.chamadoService = chamadoService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<ChamadoDTO>> listarTodos() {
        return ResponseEntity.ok(chamadoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ChamadoDTO> criarChamado(@RequestBody ChamadoDTO dto) {
        return ResponseEntity.ok(chamadoService.criarChamado(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChamadoDTO> atualizarChamado(@PathVariable Long id, @RequestBody ChamadoDTO dto) {
        return ResponseEntity.ok(chamadoService.atualizarChamado(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirChamado(@PathVariable Long id) {
        chamadoService.excluirChamado(id);
        return ResponseEntity.noContent().build();
    }

     @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ChamadoDTO>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(chamadoService.listarPorCliente(clienteId));
    }

    @GetMapping("/tecnico/{tecnicoId}")
    public ResponseEntity<List<ChamadoDTO>> listarPorTecnico(@PathVariable Long tecnicoId) {
        return ResponseEntity.ok(chamadoService.listarPorTecnico(tecnicoId));
    }

    @GetMapping("/admin/{adminId}")
    public ResponseEntity<List<ChamadoDTO>> listarPorAdmin(@PathVariable Long adminId) {
        return ResponseEntity.ok(chamadoService.listarPorAdmin(adminId));
    }

    @PutMapping("/{id}/atribuir-tecnico/{tecnicoId}")
    public ResponseEntity<ChamadoDTO> atribuirTecnico(@PathVariable Long id, @PathVariable Long tecnicoId) {
        return ResponseEntity.ok(chamadoService.atribuirTecnico(id, tecnicoId));
    }

    @PutMapping("/{id}/escalonar")
    public ResponseEntity<ChamadoDTO> escalonarChamado(@PathVariable Long id,
            @RequestBody EscalonarRequestDTO request, Authentication authentication) {
        Usuario solicitante = currentUserService.getUsuarioAtual(authentication);
        return ResponseEntity.ok(chamadoService.escalonarChamado(id, request, solicitante));
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoDTO>> listarHistorico(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.listarHistorico(id));
    }
}