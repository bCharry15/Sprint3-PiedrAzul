package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.persistence;

import java.time.DayOfWeek;
import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarDisponibilidadMedicoPort;
import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.repository.DisponibilidadMedicoRepository;

@Component
public class DisponibilidadMedicoPersistenceAdapter implements BuscarDisponibilidadMedicoPort {

    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;

    public DisponibilidadMedicoPersistenceAdapter(DisponibilidadMedicoRepository disponibilidadMedicoRepository) {
        this.disponibilidadMedicoRepository = disponibilidadMedicoRepository;
    }

    @Override
    public Optional<DisponibilidadMedico> buscarDisponibilidadActiva(Medico medico, DayOfWeek diaSemana) {
        return disponibilidadMedicoRepository
                .findFirstByMedicoAndDiaSemanaAndActivoTrueOrderByIdDesc(medico, diaSemana);
    }
}