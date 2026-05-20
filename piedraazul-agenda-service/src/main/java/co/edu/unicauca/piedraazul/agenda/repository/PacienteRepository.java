package co.edu.unicauca.piedraazul.agenda.repository;

import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByNumeroDocumento(String numeroDocumento);
}

