package com.chamado.service;

import java.util.Collection;
import java.util.List;

import com.chamado.model.Usuario;
import com.chamado.repository.UsuarioRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        return new User(usuario.getEmail(), usuario.getSenha(), mapAuthorities(usuario));
    }

    private Collection<? extends GrantedAuthority> mapAuthorities(Usuario usuario) {
        String role = "ROLE_" + usuario.getTipoUsuario().name();
        return List.of(new SimpleGrantedAuthority(role));
    }
}
