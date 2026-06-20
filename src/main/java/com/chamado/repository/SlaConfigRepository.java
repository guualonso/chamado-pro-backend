package com.chamado.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chamado.model.SlaConfig;
import com.chamado.model.enums.Prioridade;

@Repository
public interface SlaConfigRepository extends JpaRepository<SlaConfig, Long> {
    Optional<SlaConfig> findByPrioridade(Prioridade prioridade);
}
