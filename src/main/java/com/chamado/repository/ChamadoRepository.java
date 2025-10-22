    package com.chamado.repository;

    import java.util.List;

    import org.springframework.data.jpa.repository.JpaRepository;

    import com.chamado.model.Chamado;
    import com.chamado.model.Usuario;

    public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

        List<Chamado> findByCliente(Usuario cliente);

        // List<Chamado> findByTecnico(Usuario tecnico); // Para implementação futura

        // List<Chamado> findByAdmin(Usuario admin); // Para implementação futura
    }