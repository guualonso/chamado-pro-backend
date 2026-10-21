package com.chamado.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Criptografa colunas sensíveis (ex.: API Key do WhatsApp) com AES-256-GCM
 * antes de gravar no banco, e descriptografa na leitura.
 *
 * Valores gravados antes desta mudança (texto puro, sem o prefixo) continuam
 * sendo lidos normalmente e passam a ser criptografados no próximo salvamento.
 */
@Component
@Converter
public class CriptografiaConverter implements AttributeConverter<String, String> {

    private static final String PREFIXO = "enc:v1:";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec chave;
    private final SecureRandom random = new SecureRandom();

    public CriptografiaConverter(@Value("${app.security.encryption-key:}") String chaveBase64) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(chaveBase64 == null ? "" : chaveBase64.trim());
        } catch (IllegalArgumentException e) {
            bytes = new byte[0];
        }
        if (bytes.length != 32) {
            throw new IllegalStateException(
                    "APP_ENCRYPTION_KEY ausente ou inválida (esperado: 32 bytes em Base64). Gere com: openssl rand -base64 32");
        }
        this.chave = new SecretKeySpec(bytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String valor) {
        if (valor == null || valor.isEmpty()) {
            return valor;
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(valor.getBytes(StandardCharsets.UTF_8));
            byte[] saida = ByteBuffer.allocate(iv.length + cifrado.length).put(iv).put(cifrado).array();
            return PREFIXO + Base64.getEncoder().encodeToString(saida);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao criptografar dado sensível", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String valor) {
        if (valor == null || !valor.startsWith(PREFIXO)) {
            return valor;
        }
        try {
            byte[] entrada = Base64.getDecoder().decode(valor.substring(PREFIXO.length()));
            ByteBuffer buffer = ByteBuffer.wrap(entrada);
            byte[] iv = new byte[IV_BYTES];
            buffer.get(iv);
            byte[] cifrado = new byte[buffer.remaining()];
            buffer.get(cifrado);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Falha ao descriptografar dado sensível", e);
        }
    }
}
