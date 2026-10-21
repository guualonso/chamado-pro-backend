package com.chamado.exception;

/**
 * Lançada quando a requisição viola uma regra de negócio (dados inválidos,
 * operação não permitida no estado atual etc.).
 * Convertida em HTTP 400 pelo {@link GlobalExceptionHandler}.
 */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
