package co.edu.unicauca.piedraazul.agenda.repository;

import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByMedicoAndFechaOrderByHoraAsc(Medico medico, LocalDate fecha);

    boolean existsByMedicoAndFechaAndHora(Medico medico, LocalDate fecha, LocalTime hora);
}

