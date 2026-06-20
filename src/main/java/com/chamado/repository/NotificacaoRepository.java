package com.chamado.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chamado.model.Notificacao;
import com.chamado.model.Usuario;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByDestinatarioOrderByDataEnvioDesc(Usuario destinatario);
    long countByDestinatarioAndLida(Usuario destinatario, boolean lida);
}
