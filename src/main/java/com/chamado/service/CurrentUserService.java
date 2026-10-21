package com.chamado.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.chamado.model.Chamado;
import com.chamado.model.Usuario;
import com.chamado.model.enums.TipoUsuario;
import com.chamado.repository.UsuarioRepository;

/**
 * Resolve o {@link Usuario} autenticado a partir do contexto de segurança
 * e centraliza as regras de "dono do recurso".
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

    /** Igual a {@link #getUsuarioAtual}, mas nega o acesso se não houver usuário autenticado. */
    public Usuario getUsuarioObrigatorio(Authentication authentication) {
        Usuario usuario = getUsuarioAtual(authentication);
        if (usuario == null) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        return usuario;
    }

    public static boolean isAdmin(Usuario usuario) {
        return usuario != null && usuario.getTipoUsuario() == TipoUsuario.ADMIN;
    }

    public static boolean isTecnico(Usuario usuario) {
        return usuario != null && usuario.getTipoUsuario() == TipoUsuario.TECNICO;
    }

    /**
     * ADMIN e TECNICO podem ver qualquer chamado (fila de atendimento);
     * CLIENTE só pode ver os chamados que ele mesmo abriu.
     */
    public static boolean podeVerChamado(Usuario usuario, Chamado chamado) {
        if (isAdmin(usuario) || isTecnico(usuario)) {
            return true;
        }
        return usuario != null && chamado.getCliente() != null
                && chamado.getCliente().getId().equals(usuario.getId());
    }

    public static void verificarAcessoChamado(Usuario usuario, Chamado chamado) {
        if (!podeVerChamado(usuario, chamado)) {
            throw new AccessDeniedException("Acesso negado ao chamado");
        }
    }
}
