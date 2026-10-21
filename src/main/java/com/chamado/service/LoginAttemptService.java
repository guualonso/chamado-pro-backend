package com.chamado.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Limita tentativas de login malsucedidas por e-mail, bloqueando
 * temporariamente a conta para dificultar ataques de força bruta.
 *
 * O controle é em memória (por instância). Com várias instâncias atrás
 * do ALB, considere mover para um armazenamento compartilhado (ex.: Redis).
 */
@Service
public class LoginAttemptService {

    private record Tentativas(int falhas, Instant bloqueadoAte) {}

    private final Map<String, Tentativas> tentativas = new ConcurrentHashMap<>();

    @Value("${app.security.login.max-tentativas:5}")
    private int maxTentativas;

    @Value("${app.security.login.bloqueio-minutos:15}")
    private long bloqueioMinutos;

    public boolean estaBloqueado(String email) {
        Tentativas t = tentativas.get(chave(email));
        if (t == null || t.bloqueadoAte() == null) {
            return false;
        }
        if (Instant.now().isAfter(t.bloqueadoAte())) {
            tentativas.remove(chave(email));
            return false;
        }
        return true;
    }

    public void registrarFalha(String email) {
        tentativas.compute(chave(email), (k, atual) -> {
            int falhas = (atual == null ? 0 : atual.falhas()) + 1;
            Instant bloqueio = falhas >= maxTentativas
                    ? Instant.now().plus(Duration.ofMinutes(bloqueioMinutos))
                    : null;
            return new Tentativas(falhas, bloqueio);
        });
    }

    public void registrarSucesso(String email) {
        tentativas.remove(chave(email));
    }

    private String chave(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
