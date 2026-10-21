package com.chamado.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.chamado.dto.ConsentimentoRequest;
import com.chamado.dto.PerfilDTO;
import com.chamado.dto.UsuarioDTO;
import com.chamado.dto.UsuarioResumoDTO;
import com.chamado.service.CurrentUserService;
import com.chamado.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final CurrentUserService currentUserService;

    public UsuarioController(UsuarioService usuarioService, CurrentUserService currentUserService) {
        this.usuarioService = usuarioService;
        this.currentUserService = currentUserService;
    }

    // ------------------------------------------------------------------
    // Próprio usuário (qualquer perfil autenticado)
    // ------------------------------------------------------------------

    @GetMapping("/me")
    public ResponseEntity<PerfilDTO> meuPerfil(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.perfil(currentUserService.getUsuarioObrigatorio(authentication)));
    }

    @PutMapping("/me/consentimentos")
    public ResponseEntity<PerfilDTO> registrarConsentimento(@Valid @RequestBody ConsentimentoRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(usuarioService.registrarConsentimento(
                currentUserService.getUsuarioObrigatorio(authentication), request));
    }

    /** Direito de acesso/portabilidade (LGPD art. 18, II e V). */
    @GetMapping("/me/exportar")
    public ResponseEntity<Map<String, Object>> exportarMeusDados(Authentication authentication) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"meus-dados-chamadopro.json\"")
                .body(usuarioService.exportarDados(currentUserService.getUsuarioObrigatorio(authentication)));
    }

    /** Direito de eliminação (LGPD art. 18, VI): anonimiza a conta do próprio usuário. */
    @DeleteMapping("/me")
    public ResponseEntity<Void> excluirMinhaConta(Authentication authentication) {
        usuarioService.anonimizar(currentUserService.getUsuarioObrigatorio(authentication));
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Administração de usuários
    // ------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> criarUsuario(@Valid @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.criarUsuario(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * Lista os técnicos (com seus respectivos níveis N1/N2/N3) com dados mínimos.
     * Usado para popular seletores de técnico no escalonamento manual.
     */
    @GetMapping("/tecnicos")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<List<UsuarioResumoDTO>> listarTecnicos() {
        return ResponseEntity.ok(usuarioService.listarTecnicos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> atualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizarUsuario(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluirUsuario(@PathVariable Long id, Authentication authentication) {
        usuarioService.excluirUsuario(id, currentUserService.getUsuarioObrigatorio(authentication));
        return ResponseEntity.noContent().build();
    }
}
