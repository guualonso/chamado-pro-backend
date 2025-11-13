package com.chamado.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.chamado.model.Usuario;
import com.chamado.model.enums.TipoUsuario;
import com.chamado.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.findByEmail("admin@chamado.com").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNome("Administrador");
                admin.setEmail("admin@chamado.com");
                admin.setSenha(passwordEncoder.encode("admin123"));
                admin.setTipoUsuario(TipoUsuario.ADMIN);
                usuarioRepository.save(admin);
                System.out.println("✅ Usuário ADMIN criado: admin@chamado.com / admin123");
            } else {
                System.out.println("ℹ️ Usuário admin já existe, não foi recriado.");
            }
        };
    }
}
