package co.edu.unicauca.piedraazul.agenda.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import jakarta.transaction.Transactional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByMedicoAndFechaOrderByHoraAsc(Medico medico, LocalDate fecha);

    boolean existsByMedicoAndFechaAndHora(Medico medico, LocalDate fecha, LocalTime hora);

    boolean existsByMedicoAndFechaAndHoraAndIdNot(Medico medico, LocalDate fecha, LocalTime hora, Long id);

    boolean existsByMedicoId(Long medicoId);

    long countByMedicoId(Long medicoId);

    @Transactional
    void deleteByMedicoId(Long medicoId);

    List<Cita> findByPacienteNumeroDocumentoOrderByFechaAscHoraAsc(String numeroDocumento);
}