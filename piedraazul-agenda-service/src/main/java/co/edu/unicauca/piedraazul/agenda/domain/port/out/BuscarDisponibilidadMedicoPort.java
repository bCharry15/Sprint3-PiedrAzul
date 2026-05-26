package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.time.DayOfWeek;
import java.util.Optional;

import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;

public interface BuscarDisponibilidadMedicoPort {

    Optional<DisponibilidadMedico> buscarDisponibilidadActiva(Medico medico, DayOfWeek diaSemana);
}