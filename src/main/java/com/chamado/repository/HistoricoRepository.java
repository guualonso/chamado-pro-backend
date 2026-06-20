package com.chamado.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chamado.model.Chamado;
import com.chamado.model.Historico;

@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {
    List<Historico> findByChamadoOrderByDataHoraDesc(Chamado chamado);
}
