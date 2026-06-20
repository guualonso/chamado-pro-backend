package com.chamado.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chamado.model.Usuario;
import com.chamado.model.enums.NivelTecnico;
import com.chamado.model.enums.TipoUsuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);
    List<Usuario> findByTipoUsuarioAndNivelTecnico(TipoUsuario tipoUsuario, NivelTecnico nivelTecnico);
}
