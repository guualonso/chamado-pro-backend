package com.chamado.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Registro de consentimento do próprio usuário.
 * - aceiteTermos: obrigatório (Termos de Uso + Política de Privacidade).
 * - consentimentoWhatsapp: opcional (opt-in), pode ser revogado a qualquer momento.
 */
@Getter
@Setter
public class ConsentimentoRequest {

    @NotBlank
    @Size(max = 20)
    private String versaoTermos;

    @AssertTrue(message = "é necessário aceitar os Termos de Uso e a Política de Privacidade")
    private boolean aceiteTermos;

    private boolean consentimentoWhatsapp;
}
