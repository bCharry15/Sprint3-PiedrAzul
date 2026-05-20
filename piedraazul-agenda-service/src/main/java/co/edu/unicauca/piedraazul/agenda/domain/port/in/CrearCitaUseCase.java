package co.edu.unicauca.piedraazul.agenda.domain.port.in;

import co.edu.unicauca.piedraazul.agenda.application.dto.CrearCitaCommand;
import co.edu.unicauca.piedraazul.agenda.application.dto.CitaResponse;

public interface CrearCitaUseCase {

    CitaResponse crearCita(CrearCitaCommand command);
}