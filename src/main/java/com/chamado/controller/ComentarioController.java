package com.chamado.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.chamado.dto.ComentarioDTO;
import com.chamado.service.ComentarioService;
import com.chamado.service.CurrentUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final CurrentUserService currentUserService;

    public ComentarioController(ComentarioService comentarioService, CurrentUserService currentUserService) {
        this.comentarioService = comentarioService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/chamado/{chamadoId}")
    public ResponseEntity<List<ComentarioDTO>> listarPorChamado(@PathVariable Long chamadoId, Authentication authentication) {
        return ResponseEntity.ok(comentarioService.listarPorChamado(chamadoId,
                currentUserService.getUsuarioObrigatorio(authentication)));
    }

    @PostMapping("/chamado/{chamadoId}")
    public ResponseEntity<ComentarioDTO> criarComentario(@PathVariable Long chamadoId,
            @Valid @RequestBody ComentarioDTO dto, Authentication authentication) {
        return ResponseEntity.ok(comentarioService.criarComentario(chamadoId, dto,
                currentUserService.getUsuarioObrigatorio(authentication)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirComentario(@PathVariable Long id, Authentication authentication) {
        comentarioService.excluirComentario(id, currentUserService.getUsuarioObrigatorio(authentication));
        return ResponseEntity.noContent().build();
    }
}
