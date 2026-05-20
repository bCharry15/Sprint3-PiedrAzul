package co.edu.unicauca.piedraazul.agenda.repository;

import co.edu.unicauca.piedraazul.agenda.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    Optional<Medico> findByUserUsername(String username);
}

