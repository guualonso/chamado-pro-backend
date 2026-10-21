package com.chamado.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final CurrentUserService currentUserService;

    public ChamadoController(ChamadoService chamadoService, CurrentUserService currentUserService) {
        this.chamadoService = chamadoService;
        this.currentUserService = currentUserService;
    }

    /** Fila completa de chamados: apenas equipe de atendimento. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<List<ChamadoDTO>> listarTodos() {
        return ResponseEntity.ok(chamadoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoDTO> buscarPorId(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(chamadoService.buscarPorId(id, usuario(authentication)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<ChamadoDTO> criarChamado(@Valid @RequestBody ChamadoDTO dto, Authentication authentication) {
        return ResponseEntity.ok(chamadoService.criarChamado(dto, usuario(authentication)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChamadoDTO> atualizarChamado(@PathVariable Long id, @Valid @RequestBody ChamadoDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(chamadoService.atualizarChamado(id, dto, usuario(authentication)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluirChamado(@PathVariable Long id) {
        chamadoService.excluirChamado(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ChamadoDTO>> listarPorCliente(@PathVariable Long clienteId, Authentication authentication) {
        return ResponseEntity.ok(chamadoService.listarPorCliente(clienteId, usuario(authentication)));
    }

    @GetMapping("/tecnico/{tecnicoId}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<List<ChamadoDTO>> listarPorTecnico(@PathVariable Long tecnicoId, Authentication authentication) {
        return ResponseEntity.ok(chamadoService.listarPorTecnico(tecnicoId, usuario(authentication)));
    }

    @GetMapping("/admin/{adminId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChamadoDTO>> listarPorAdmin(@PathVariable Long adminId) {
        return ResponseEntity.ok(chamadoService.listarPorAdmin(adminId));
    }

    @PutMapping("/{id}/atribuir-tecnico/{tecnicoId}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<ChamadoDTO> atribuirTecnico(@PathVariable Long id, @PathVariable Long tecnicoId,
            Authentication authentication) {
        return ResponseEntity.ok(chamadoService.atribuirTecnico(id, tecnicoId, usuario(authentication)));
    }

    @PutMapping("/{id}/escalonar")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<ChamadoDTO> escalonarChamado(@PathVariable Long id,
            @Valid @RequestBody EscalonarRequestDTO request, Authentication authentication) {
        return ResponseEntity.ok(chamadoService.escalonarChamado(id, request, usuario(authentication)));
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoDTO>> listarHistorico(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(chamadoService.listarHistorico(id, usuario(authentication)));
    }

    private Usuario usuario(Authentication authentication) {
        return currentUserService.getUsuarioObrigatorio(authentication);
    }
}
