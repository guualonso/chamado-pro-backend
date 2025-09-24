package com.chamado.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chamado.dto.ChamadoDTO;
// import com.chamado.model.Usuario; // Para implementação futura
import com.chamado.service.ChamadoService;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;

    public ChamadoController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
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

    /*
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Chamado>> listarPorCliente(@PathVariable Long clienteId) {
        Usuario cliente = new Usuario();
        cliente.setId(clienteId);
        return ResponseEntity.ok(chamadoService.listarPorCliente(cliente));
    }

    @PutMapping("/{id}/atribuir-tecnico/{tecnicoId}")
    public ResponseEntity<Chamado> atribuirTecnico(@PathVariable Long id, @PathVariable Long tecnicoId) {
        Usuario tecnico = new Usuario();
        tecnico.setId(tecnicoId);
        return ResponseEntity.ok(chamadoService.atribuirTecnico(id, tecnico));
    }
    */
}