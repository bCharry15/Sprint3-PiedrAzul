package co.edu.unicauca.piedraazul.agenda.domain.port.in;

import java.util.List;

import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.dto.MedicoRequest;

public interface GestionarMedicosUseCase {

    List<Medico> listarTodos();

    Medico crearMedico(MedicoRequest request);

    Medico obtenerPorId(Long medicoId);

    Medico actualizarMedico(Long medicoId, MedicoRequest request);

    void eliminarMedico(Long medicoId);
}