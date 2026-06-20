package com.chamado.service;

import java.util.List;

import com.chamado.dto.NotificacaoDTO;
import com.chamado.model.Chamado;
import com.chamado.model.Usuario;

public interface NotificacaoService {

    /**
     * Registra e envia uma notificação para o destinatário informado.
     * Se o destinatário tiver telefone e API Key de WhatsApp configurados,
     * a mensagem também é enviada via CallMeBot; caso contrário fica
     * disponível apenas no painel de notificações do sistema.
     */
    void notificar(Usuario destinatario, Chamado chamado, String mensagem);

    /**
     * Lista as notificações de um usuário, mais recentes primeiro.
     */
    List<NotificacaoDTO> listarPorUsuario(Usuario usuario);

    /**
     * Conta quantas notificações não lidas um usuário possui.
     */
    long contarNaoLidas(Usuario usuario);

    /**
     * Marca uma notificação como lida (somente se pertencer ao usuário informado).
     */
    void marcarComoLida(Long id, Usuario usuario);
}
