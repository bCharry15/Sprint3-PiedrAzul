package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.piedraazul.agenda.model.Medico;

public interface GestionarMedicosPort {

    List<Medico> listarTodos();

    Optional<Medico> buscarPorId(Long medicoId);

    Medico guardar(Medico medico);
}