package co.edu.unicauca.piedraazul.agenda.repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import jakarta.transaction.Transactional;

public interface DisponibilidadMedicoRepository extends JpaRepository<DisponibilidadMedico, Long> {

    Optional<DisponibilidadMedico> findByMedicoIdAndDiaSemanaAndActivoTrue(Long medicoId, DayOfWeek diaSemana);

    Optional<DisponibilidadMedico> findFirstByMedicoAndDiaSemanaAndActivoTrueOrderByIdDesc(
            Medico medico,
            DayOfWeek diaSemana
    );

    List<DisponibilidadMedico> findByMedicoAndActivoTrue(Medico medico);

    List<DisponibilidadMedico> findByMedicoId(Long medicoId);

    @Transactional
    void deleteByMedicoId(Long medicoId);
}