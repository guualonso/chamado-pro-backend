package com.chamado.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chamado.dto.ComentarioDTO;
import com.chamado.service.ComentarioService;

@RestController
@RequestMapping("/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping("/chamado/{chamadoId}")
    public ResponseEntity<List<ComentarioDTO>> listarPorChamado(@PathVariable Long chamadoId) {
        return ResponseEntity.ok(comentarioService.listarPorChamado(chamadoId));
    }

    @PostMapping("/chamado/{chamadoId}")
    public ResponseEntity<ComentarioDTO> criarComentario(@PathVariable Long chamadoId, @RequestBody ComentarioDTO dto) {
        return ResponseEntity.ok(comentarioService.criarComentario(chamadoId, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirComentario(@PathVariable Long id) {
        comentarioService.excluirComentario(id);
        return ResponseEntity.noContent().build();
    }
}
