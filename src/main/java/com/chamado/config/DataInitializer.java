package com.chamado.config;

import java.security.SecureRandom;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.chamado.model.Usuario;
import com.chamado.model.enums.TipoUsuario;
import com.chamado.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.admin.email:admin@chamado.com}")
    private String adminEmail;

    @Value("${app.admin.senha:}")
    private String adminSenha;

    @Bean
    CommandLineRunner initAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.findByEmail(adminEmail).isPresent()) {
                log.info("Usuário admin já existe, não foi recriado.");
                return;
            }

            boolean senhaGerada = adminSenha == null || adminSenha.isBlank();
            String senha = senhaGerada ? gerarSenhaAleatoria() : adminSenha;

            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setEmail(adminEmail);
            admin.setSenha(passwordEncoder.encode(senha));
            admin.setTipoUsuario(TipoUsuario.ADMIN);
            usuarioRepository.save(admin);

            if (senhaGerada) {
                // Exibida uma única vez, apenas quando o admin é criado. Troque-a no primeiro acesso.
                log.warn("Usuário ADMIN criado: {} | senha gerada (troque após o primeiro acesso): {}", adminEmail, senha);
            } else {
                log.info("Usuário ADMIN criado: {} (senha definida em ADMIN_DEFAULT_PASSWORD)", adminEmail);
            }
        };
    }

    private static String gerarSenhaAleatoria() {
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
