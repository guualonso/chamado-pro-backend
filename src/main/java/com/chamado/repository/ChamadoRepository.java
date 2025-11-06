    package com.chamado.repository;

    import java.util.List;

    import org.springframework.data.jpa.repository.JpaRepository;

    import com.chamado.model.Chamado;
    import com.chamado.model.Usuario;

    public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

        List<Chamado> findByCliente(Usuario cliente);
        List<Chamado> findByTecnico(Usuario tecnico); 
        List<Chamado> findByAdmin(Usuario admin);
        List<Chamado> findByStatus(StatusChamado status);
    }