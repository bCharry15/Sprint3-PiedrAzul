package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.util.List;

import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;

public interface ConfigurarDisponibilidadPort {

    DisponibilidadMedico guardar(DisponibilidadMedico disponibilidad);

    List<DisponibilidadMedico> buscarPorMedicoActivo(Medico medico);
}