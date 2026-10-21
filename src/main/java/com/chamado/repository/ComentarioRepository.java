package com.chamado.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.chamado.model.Comentario;
import com.chamado.model.Chamado;
import com.chamado.model.Usuario;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByChamado(Chamado chamado);
    List<Comentario> findByAutor(Usuario autor);
}
