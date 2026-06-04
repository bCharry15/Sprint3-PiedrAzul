package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;

public interface ConfigurarDisponibilidadPort {

    DisponibilidadMedico guardar(DisponibilidadMedico disponibilidad);

    Optional<DisponibilidadMedico> buscarPorId(Long disponibilidadId);

    List<DisponibilidadMedico> buscarPorMedicoActivo(Medico medico);
}