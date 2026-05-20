package co.edu.unicauca.piedraazul.agenda.repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;

public interface DisponibilidadMedicoRepository extends JpaRepository<DisponibilidadMedico, Long> {

    List<DisponibilidadMedico> findByMedicoAndActivoTrue(Medico medico);

    Optional<DisponibilidadMedico> findFirstByMedicoAndDiaSemanaAndActivoTrue(
            Medico medico,
            DayOfWeek diaSemana
    );

    Optional<DisponibilidadMedico> findFirstByMedicoAndDiaSemanaAndActivoTrueOrderByIdDesc(
            Medico medico,
            DayOfWeek diaSemana
    );
}