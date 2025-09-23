package com.chamado.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chamado.model.Chamado;
//import com.chamado.model.Usuario;
import com.chamado.service.ChamadoService;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;

    public ChamadoController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @GetMapping
    public ResponseEntity<List<Chamado>> listarTodos() {
        return ResponseEntity.ok(chamadoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<Chamado> criarChamado(@RequestBody Chamado chamado) {
        return ResponseEntity.ok(chamadoService.criarChamado(chamado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Chamado> atualizarChamado(@PathVariable Long id, @RequestBody Chamado chamado) {
        return ResponseEntity.ok(chamadoService.atualizarChamado(id, chamado));
    }

    /**@PutMapping("/{id}/atribuir-tecnico/{tecnicoId}")
    public ResponseEntity<Chamado> atribuirTecnico(@PathVariable Long id, @PathVariable Long tecnicoId) {
        Usuario tecnico = new Usuario();
        tecnico.setId(tecnicoId);
        return ResponseEntity.ok(chamadoService.atribuirTecnico(id, tecnico));
    }**/
}