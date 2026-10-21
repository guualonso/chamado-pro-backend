package com.chamado.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.chamado.config.JwtUtil;
import com.chamado.model.Usuario;
import com.chamado.repository.UsuarioRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final LoginAttemptService loginAttemptService;

    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            UsuarioRepository usuarioRepository, LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.loginAttemptService = loginAttemptService;
    }

    public String authenticateAndGetToken(String email, String senha) {
        if (loginAttemptService.estaBloqueado(email)) {
            throw new LockedException("Conta temporariamente bloqueada");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, senha)
            );
        } catch (AuthenticationException e) {
            loginAttemptService.registrarFalha(email);
            throw new BadCredentialsException("Credenciais inválidas");
        }

        loginAttemptService.registrarSucesso(email);

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        return jwtUtil.gerarToken(usuario.getEmail(), usuario.getTipoUsuario().name());
    }
}
