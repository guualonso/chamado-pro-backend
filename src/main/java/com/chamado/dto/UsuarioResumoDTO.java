package com.chamado.dto;

import com.chamado.model.enums.NivelTecnico;

/**
 * Dados mínimos de um técnico, usados nos seletores de atribuição/escalonamento.
 * Não expõe e-mail, telefone ou outros dados pessoais desnecessários.
 */
public record UsuarioResumoDTO(Long id, String nome, NivelTecnico nivelTecnico) {
}
