package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.CambiarEstadoCitaUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarCitasPort;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.dto.CambiarEstadoCitaRequest;
import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;
import jakarta.transaction.Transactional;

@Service
public class CambiarEstadoCitaService implements CambiarEstadoCitaUseCase {

    private final BuscarCitasPort buscarCitasPort;

    public CambiarEstadoCitaService(BuscarCitasPort buscarCitasPort) {
        this.buscarCitasPort = buscarCitasPort;
    }

    @Override
    @Transactional
    public Map<String, Object> cambiarEstado(Long citaId, CambiarEstadoCitaRequest request) {
        validarIdCita(citaId);
        validarSolicitud(request);

        Cita cita = buscarCitasPort.buscarPorId(citaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe una cita con id: " + citaId
                ));

        EstadoCita nuevoEstado = convertirEstado(request.getEstado());

        validarCambioEstado(cita.getEstado(), nuevoEstado);

        EstadoCita estadoAnterior = cita.getEstado();

        cita.setEstado(nuevoEstado);

        if (request.getObservacion() != null && !request.getObservacion().trim().isEmpty()) {
            cita.setObservacion(construirObservacionCambioEstado(cita.getObservacion(), estadoAnterior, nuevoEstado, request.getObservacion()));
        }

        Cita citaActualizada = buscarCitasPort.guardar(cita);

        Map<String, Object> response = convertirCitaAMap(citaActualizada);
        response.put("mensaje", "Estado de cita actualizado correctamente.");
        response.put("estadoAnterior", estadoAnterior.name());
        response.put("estadoNuevo", nuevoEstado.name());

        return response;
    }

    private void validarIdCita(Long citaId) {
        if (citaId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El id de la cita es obligatorio."
            );
        }
    }

    private void validarSolicitud(CambiarEstadoCitaRequest request) {
        if (request == null || request.getEstado() == null || request.getEstado().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nuevo estado de la cita es obligatorio."
            );
        }
    }

    private EstadoCita convertirEstado(String estado) {
        try {
            return EstadoCita.valueOf(estado.trim().toUpperCase());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado inválido. Valores permitidos: CONFIRMADA, ATENDIDA, CANCELADA, NO_VINO, COMPLETADA."
            );
        }
    }

    private void validarCambioEstado(EstadoCita estadoActual, EstadoCita nuevoEstado) {
        if (nuevoEstado == EstadoCita.PROGRAMADA || nuevoEstado == EstadoCita.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se permite devolver una cita a estado PROGRAMADA o PENDIENTE desde este endpoint."
            );
        }

        if (estadoActual == EstadoCita.ATENDIDA
                || estadoActual == EstadoCita.COMPLETADA
                || estadoActual == EstadoCita.CANCELADA
                || estadoActual == EstadoCita.NO_VINO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La cita ya está en un estado final y no puede ser modificada."
            );
        }

        if (nuevoEstado != EstadoCita.CONFIRMADA
                && nuevoEstado != EstadoCita.ATENDIDA
                && nuevoEstado != EstadoCita.COMPLETADA
                && nuevoEstado != EstadoCita.CANCELADA
                && nuevoEstado != EstadoCita.NO_VINO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado no permitido para cambio de cita."
            );
        }
    }

    private String construirObservacionCambioEstado(String observacionActual,
                                                    EstadoCita estadoAnterior,
                                                    EstadoCita estadoNuevo,
                                                    String observacionNueva) {
        String observacionBase = observacionActual == null ? "" : observacionActual.trim();

        String textoCambio = "Cambio de estado de "
                + estadoAnterior.name()
                + " a "
                + estadoNuevo.name()
                + ". Observación: "
                + observacionNueva.trim();

        if (observacionBase.isBlank()) {
            return textoCambio;
        }

        return observacionBase + " | " + textoCambio;
    }

    private Map<String, Object> convertirCitaAMap(Cita cita) {
        Map<String, Object> response = new HashMap<>();

        response.put("id", cita.getId());
        response.put("pacienteId", cita.getPaciente().getId());
        response.put("paciente", cita.getPaciente().getNombreCompleto());
        response.put("documento", cita.getPaciente().getNumeroDocumento());
        response.put("medicoId", cita.getMedico().getId());
        response.put("medico", cita.getMedico().getNombreCompleto());
        response.put("fecha", cita.getFecha());
        response.put("hora", cita.getHora());
        response.put("estado", cita.getEstado().name());
        response.put("observacion", cita.getObservacion());

        return response;
    }
}