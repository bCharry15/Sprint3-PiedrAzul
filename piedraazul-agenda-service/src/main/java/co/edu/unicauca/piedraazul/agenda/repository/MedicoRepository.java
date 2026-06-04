package co.edu.unicauca.piedraazul.agenda.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.piedraazul.agenda.model.Medico;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    List<Medico> findByActivoTrueOrderByNombreCompletoAsc();

    Optional<Medico> findByIdAndActivoTrue(Long id);

    Optional<Medico> findByUserUsername(String username);

    Optional<Medico> findByUserUsernameAndActivoTrue(String username);
}