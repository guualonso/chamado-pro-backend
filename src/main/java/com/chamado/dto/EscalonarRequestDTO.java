package com.chamado.dto;

import com.chamado.model.enums.NivelTecnico;

import lombok.Getter;
import lombok.Setter;

/**
 * Corpo da requisição para escalonamento manual de um chamado.
 * Todos os campos são opcionais:
 * - novoNivel: se nulo, escalona automaticamente para o próximo nível (N1->N2->N3).
 * - tecnicoId: se informado, atribui o chamado a esse técnico específico.
 * - motivo: texto livre registrado no histórico do chamado.
 */
@Getter
@Setter
public class EscalonarRequestDTO {
    private NivelTecnico novoNivel;
    private Long tecnicoId;
    private String motivo;
}
