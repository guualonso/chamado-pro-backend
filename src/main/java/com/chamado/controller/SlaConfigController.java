package com.chamado.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chamado.dto.SlaConfigDTO;
import com.chamado.model.enums.Prioridade;
import com.chamado.service.SlaService;

/**
 * Endpoints administrativos para visualizar e editar os prazos de SLA
 * (primeira resposta e resolução) por nível de prioridade.
 */
@RestController
@RequestMapping("/sla-config")
public class SlaConfigController {

    private final SlaService slaService;

    public SlaConfigController(SlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping
    public ResponseEntity<List<SlaConfigDTO>> listar() {
        return ResponseEntity.ok(slaService.listarConfiguracoes());
    }

    @PutMapping("/{prioridade}")
    public ResponseEntity<SlaConfigDTO> atualizar(@PathVariable Prioridade prioridade,
                                                    @RequestBody SlaConfigDTO dto) {
        return ResponseEntity.ok(slaService.atualizarConfiguracao(prioridade, dto));
    }
}
