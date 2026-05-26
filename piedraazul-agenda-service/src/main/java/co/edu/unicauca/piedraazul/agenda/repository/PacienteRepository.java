package co.edu.unicauca.piedraazul.agenda.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.piedraazul.agenda.model.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByNumeroDocumento(String numeroDocumento);

    Optional<Paciente> findByUsername(String username);

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByUsername(String username);
}