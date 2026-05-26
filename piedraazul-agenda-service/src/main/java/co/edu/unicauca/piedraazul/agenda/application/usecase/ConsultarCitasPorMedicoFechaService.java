package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.ConsultarCitasPorMedicoFechaUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarCitasPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarMedicoPort;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.dto.CitaResponse;
import co.edu.unicauca.piedraazul.agenda.model.dto.CitasPorMedicoFechaResponse;

@Service
public class ConsultarCitasPorMedicoFechaService implements ConsultarCitasPorMedicoFechaUseCase {

    private final BuscarMedicoPort buscarMedicoPort;
    private final BuscarCitasPort buscarCitasPort;

    public ConsultarCitasPorMedicoFechaService(BuscarMedicoPort buscarMedicoPort,
                                               BuscarCitasPort buscarCitasPort) {
        this.buscarMedicoPort = buscarMedicoPort;
        this.buscarCitasPort = buscarCitasPort;
    }

    @Override
    public CitasPorMedicoFechaResponse consultar(Long medicoId, LocalDate fecha) {
        if (medicoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El id del médico/terapista es obligatorio."
            );
        }

        if (fecha == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de consulta es obligatoria."
            );
        }

        Medico medico = buscarMedicoPort.buscarPorId(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista con id: " + medicoId
                ));

        List<Cita> citas = buscarCitasPort.buscarPorMedicoYFecha(medico, fecha);

        List<CitaResponse> citasResponse = citas.stream()
                .map(this::convertirACitaResponse)
                .toList();

        return new CitasPorMedicoFechaResponse(
                medico.getId(),
                medico.getNombreCompleto(),
                fecha,
                citasResponse.size(),
                citasResponse
        );
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