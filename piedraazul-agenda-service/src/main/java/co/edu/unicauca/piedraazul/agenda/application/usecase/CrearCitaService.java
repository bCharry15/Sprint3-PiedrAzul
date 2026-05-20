package co.edu.unicauca.piedraazul.agenda.application.usecase;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.piedraazul.agenda.application.dto.CitaResponse;
import co.edu.unicauca.piedraazul.agenda.application.dto.CrearCitaCommand;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.CrearCitaUseCase;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.dto.CrearCitaRequest;
import co.edu.unicauca.piedraazul.agenda.pattern.facade.AgendaFacade;

@Service
public class CrearCitaService implements CrearCitaUseCase {

    private final AgendaFacade agendaFacade;
    private final ObjectMapper objectMapper;

    public CrearCitaService(AgendaFacade agendaFacade, ObjectMapper objectMapper) {
        this.agendaFacade = agendaFacade;
        this.objectMapper = objectMapper;
    }

    @Override
    public CitaResponse crearCita(CrearCitaCommand command) {
        CrearCitaRequest request = objectMapper.convertValue(command, CrearCitaRequest.class);

        Cita citaCreada = agendaFacade.crearCitaDesdeSolicitud(request);

        return new CitaResponse(
                citaCreada.getId(),
                command.getNumeroDocumento(),
                citaCreada.getPaciente().getNombreCompleto(),
                citaCreada.getMedico().getNombreCompleto(),
                citaCreada.getFecha(),
                citaCreada.getHora(),
                citaCreada.getEstado().name()
        );
    }
}