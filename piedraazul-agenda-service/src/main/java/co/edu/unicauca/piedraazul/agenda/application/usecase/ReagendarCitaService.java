package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.ReagendarCitaUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarCitasPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.GuardarHistorialReagendamientoPort;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.HistorialReagendamiento;
import co.edu.unicauca.piedraazul.agenda.model.dto.ReagendarCitaRequest;
import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;

import jakarta.transaction.Transactional;

@Service
public class ReagendarCitaService implements ReagendarCitaUseCase {

    private final BuscarCitasPort buscarCitasPort;
    private final GuardarHistorialReagendamientoPort guardarHistorialReagendamientoPort;

    public ReagendarCitaService(BuscarCitasPort buscarCitasPort,
                                GuardarHistorialReagendamientoPort guardarHistorialReagendamientoPort) {
        this.buscarCitasPort = buscarCitasPort;
        this.guardarHistorialReagendamientoPort = guardarHistorialReagendamientoPort;
    }

    @Override
    @Transactional
    public Map<String, Object> reagendar(Long citaId, ReagendarCitaRequest request) {
        validarIdCita(citaId);
        validarSolicitud(request);

        Cita cita = buscarCitasPort.buscarPorId(citaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe una cita con id: " + citaId
                ));

        validarCitaReagendable(cita);

        LocalDate fechaAnterior = cita.getFecha();
        LocalTime horaAnterior = cita.getHora();

        LocalDate fechaNueva = request.getFechaNueva();
        LocalTime horaNueva = request.getHoraNueva();

        validarFechaHoraNueva(fechaAnterior, horaAnterior, fechaNueva, horaNueva);
        validarHorarioDisponible(cita, fechaNueva, horaNueva);

        HistorialReagendamiento historial = crearHistorial(
                cita,
                fechaAnterior,
                horaAnterior,
                fechaNueva,
                horaNueva,
                request
        );

        guardarHistorialReagendamientoPort.guardar(historial);

        cita.setFecha(fechaNueva);
        cita.setHora(horaNueva);
        cita.setObservacion(construirObservacionReagendamiento(cita, historial));

        Cita citaActualizada = buscarCitasPort.guardar(cita);

        Map<String, Object> response = convertirCitaAMap(citaActualizada);
        response.put("mensaje", "Cita re-agendada correctamente.");
        response.put("fechaAnterior", fechaAnterior);
        response.put("horaAnterior", horaAnterior);
        response.put("fechaNueva", fechaNueva);
        response.put("horaNueva", horaNueva);

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

    private void validarSolicitud(ReagendarCitaRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La solicitud de re-agendamiento no puede estar vacía."
            );
        }

        if (request.getFechaNueva() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La nueva fecha de la cita es obligatoria."
            );
        }

        if (request.getHoraNueva() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La nueva hora de la cita es obligatoria."
            );
        }
    }

    private void validarCitaReagendable(Cita cita) {
        if (cita.getEstado() == EstadoCita.ATENDIDA
                || cita.getEstado() == EstadoCita.COMPLETADA
                || cita.getEstado() == EstadoCita.CANCELADA
                || cita.getEstado() == EstadoCita.NO_VINO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La cita ya está en un estado final y no puede ser re-agendada."
            );
        }
    }

    private void validarFechaHoraNueva(LocalDate fechaAnterior,
                                       LocalTime horaAnterior,
                                       LocalDate fechaNueva,
                                       LocalTime horaNueva) {
        if (fechaNueva.equals(fechaAnterior) && horaNueva.equals(horaAnterior)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La nueva fecha y hora son iguales a la fecha y hora actual de la cita."
            );
        }

        if (fechaNueva.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede re-agendar una cita para una fecha pasada."
            );
        }
    }

    private void validarHorarioDisponible(Cita cita, LocalDate fechaNueva, LocalTime horaNueva) {
        boolean horarioOcupado = buscarCitasPort.existeHorarioOcupadoDiferenteDeCita(
                cita.getMedico(),
                fechaNueva,
                horaNueva,
                cita.getId()
        );

        if (horarioOcupado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El médico ya tiene una cita registrada en la nueva fecha y hora seleccionada."
            );
        }
    }

    private HistorialReagendamiento crearHistorial(Cita cita,
                                                   LocalDate fechaAnterior,
                                                   LocalTime horaAnterior,
                                                   LocalDate fechaNueva,
                                                   LocalTime horaNueva,
                                                   ReagendarCitaRequest request) {
        HistorialReagendamiento historial = new HistorialReagendamiento();

        historial.setCita(cita);
        historial.setFechaAnterior(fechaAnterior);
        historial.setHoraAnterior(horaAnterior);
        historial.setFechaNueva(fechaNueva);
        historial.setHoraNueva(horaNueva);
        historial.setResponsable(normalizarTextoOpcional(request.getResponsable(), "Sistema"));
        historial.setMotivo(normalizarTextoOpcional(request.getMotivo(), "Re-agendamiento de cita"));
        historial.setFechaCambio(LocalDateTime.now());

        return historial;
    }

    private String construirObservacionReagendamiento(Cita cita, HistorialReagendamiento historial) {
        String observacionActual = cita.getObservacion() == null ? "" : cita.getObservacion().trim();

        String nuevaObservacion = "Re-agendada de "
                + historial.getFechaAnterior() + " " + historial.getHoraAnterior()
                + " a " + historial.getFechaNueva() + " " + historial.getHoraNueva()
                + ". Responsable: " + historial.getResponsable()
                + ". Motivo: " + historial.getMotivo();

        if (observacionActual.isBlank()) {
            return nuevaObservacion;
        }

        return observacionActual + " | " + nuevaObservacion;
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

    private String normalizarTextoOpcional(String valor, String valorPorDefecto) {
        if (valor == null || valor.trim().isEmpty()) {
            return valorPorDefecto;
        }

        return valor.trim().replaceAll("\\s+", " ");
    }
}