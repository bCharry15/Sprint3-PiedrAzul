package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.util.Optional;

import co.edu.unicauca.piedraazul.agenda.model.Medico;

public interface BuscarMedicoPort {

    Optional<Medico> buscarPorId(Long medicoId);
}