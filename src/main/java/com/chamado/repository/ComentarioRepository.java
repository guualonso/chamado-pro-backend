package com.chamado.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.chamado.model.Comentario;
import com.chamado.model.Chamado;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByChamado(Chamado chamado);
}
