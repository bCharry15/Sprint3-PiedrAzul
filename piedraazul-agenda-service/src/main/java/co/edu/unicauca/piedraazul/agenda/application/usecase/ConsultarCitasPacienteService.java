package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.ConsultarCitasPacienteUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarCitasPort;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.dto.CitaResponse;

@Service
public class ConsultarCitasPacienteService implements ConsultarCitasPacienteUseCase {

    private final BuscarCitasPort buscarCitasPort;

    public ConsultarCitasPacienteService(BuscarCitasPort buscarCitasPort) {
        this.buscarCitasPort = buscarCitasPort;
    }

    @Override
    public List<CitaResponse> consultarPorNumeroDocumento(String numeroDocumento) {
        return buscarCitasPort.buscarPorNumeroDocumentoPaciente(numeroDocumento)
                .stream()
                .map(this::convertirACitaResponse)
                .toList();
    }

    private CitaResponse convertirACitaResponse(Cita cita) {
        CitaResponse response = new CitaResponse();

        response.setId(cita.getId());
        response.setPacienteId(cita.getPaciente().getId());
        response.setPaciente(cita.getPaciente().getNombreCompleto());
        response.setMedicoId(cita.getMedico().getId());
        response.setMedico(cita.getMedico().getNombreCompleto());
        response.setFecha(cita.getFecha());
        response.setHora(cita.getHora());
        response.setEstado(cita.getEstado().name());
        response.setObservacion(cita.getObservacion());

        return response;
    }
}