package com.chamado.exception;

/**
 * Lançada quando um recurso (chamado, usuário, comentário...) não existe.
 * Convertida em HTTP 404 pelo {@link GlobalExceptionHandler}.
 */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
