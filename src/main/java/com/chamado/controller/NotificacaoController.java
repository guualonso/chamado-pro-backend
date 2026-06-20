package com.chamado.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chamado.dto.NotificacaoDTO;
import com.chamado.model.Usuario;
import com.chamado.service.CurrentUserService;
import com.chamado.service.NotificacaoService;


@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final CurrentUserService currentUserService;

    public NotificacaoController(NotificacaoService notificacaoService, CurrentUserService currentUserService) {
        this.notificacaoService = notificacaoService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoDTO>> listar(Authentication authentication) {
        Usuario usuario = currentUserService.getUsuarioAtual(authentication);
        return ResponseEntity.ok(notificacaoService.listarPorUsuario(usuario));
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<Long> contarNaoLidas(Authentication authentication) {
        Usuario usuario = currentUserService.getUsuarioAtual(authentication);
        return ResponseEntity.ok(notificacaoService.contarNaoLidas(usuario));
    }

    @PutMapping("/{id}/lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = currentUserService.getUsuarioAtual(authentication);
        notificacaoService.marcarComoLida(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
