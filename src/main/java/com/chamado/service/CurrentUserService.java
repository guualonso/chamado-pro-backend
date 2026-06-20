package com.chamado.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.chamado.model.Usuario;
import com.chamado.repository.UsuarioRepository;

/**
 * Resolve o {@link Usuario} autenticado a partir do contexto de segurança.
 * O "username" usado no JWT/UserDetails é o e-mail do usuário.
 */
@Service
public class CurrentUserService {

    private final UsuarioRepository usuarioRepository;

    public CurrentUserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario getUsuarioAtual(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return usuarioRepository.findByEmail(authentication.getName()).orElse(null);
    }
}
