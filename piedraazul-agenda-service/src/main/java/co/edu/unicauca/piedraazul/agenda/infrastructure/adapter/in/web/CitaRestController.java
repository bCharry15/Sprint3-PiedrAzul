package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.application.dto.CrearCitaCommand;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.ConsultarCitasPacienteUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.ConsultarCitasPorMedicoFechaUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.CrearCitaUseCase;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.HistorialReagendamiento;
import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;
import co.edu.unicauca.piedraazul.agenda.model.dto.CitaResponse;
import co.edu.unicauca.piedraazul.agenda.model.dto.CitasPorMedicoFechaResponse;
import co.edu.unicauca.piedraazul.agenda.repository.CitaRepository;
import co.edu.unicauca.piedraazul.agenda.repository.HistorialReagendamientoRepository;

@RestController
@RequestMapping("/api/citas")
public class CitaRestController {

    private final ConsultarCitasPorMedicoFechaUseCase consultarCitasPorMedicoFechaUseCase;
    private final CrearCitaUseCase crearCitaUseCase;
    private final ConsultarCitasPacienteUseCase consultarCitasPacienteUseCase;
    private final CitaRepository citaRepository;
    private final HistorialReagendamientoRepository historialReagendamientoRepository;

    public CitaRestController(ConsultarCitasPorMedicoFechaUseCase consultarCitasPorMedicoFechaUseCase,
                              CrearCitaUseCase crearCitaUseCase,
                              ConsultarCitasPacienteUseCase consultarCitasPacienteUseCase,
                              CitaRepository citaRepository,
                              HistorialReagendamientoRepository historialReagendamientoRepository) {
        this.consultarCitasPorMedicoFechaUseCase = consultarCitasPorMedicoFechaUseCase;
        this.crearCitaUseCase = crearCitaUseCase;
        this.consultarCitasPacienteUseCase = consultarCitasPacienteUseCase;
        this.citaRepository = citaRepository;
        this.historialReagendamientoRepository = historialReagendamientoRepository;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CitasPorMedicoFechaResponse listarPorMedicoYFecha(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return consultarCitasPorMedicoFechaUseCase.consultar(medicoId, fecha);
    }

    @GetMapping("/paciente/{numeroDocumento}")
    @ResponseStatus(HttpStatus.OK)
    public List<CitaResponse> listarPorPaciente(
            @PathVariable String numeroDocumento
    ) {
        return consultarCitasPacienteUseCase.consultarPorNumeroDocumento(numeroDocumento);
    }

    @GetMapping(value = "/exportar", produces = "text/csv")
    public ResponseEntity<String> exportarCitasPorMedicoYFecha(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        CitasPorMedicoFechaResponse response = consultarCitasPorMedicoFechaUseCase.consultar(medicoId, fecha);

        StringBuilder csv = new StringBuilder();
        csv.append("ID Cita,Paciente,Medico,Fecha,Hora,Estado,Observacion\n");

        response.getCitas().forEach(cita -> {
            csv.append(cita.getId()).append(",");
            csv.append(escaparCsv(cita.getPaciente())).append(",");
            csv.append(escaparCsv(cita.getMedico())).append(",");
            csv.append(cita.getFecha()).append(",");
            csv.append(cita.getHora()).append(",");
            csv.append(escaparCsv(cita.getEstado())).append(",");
            csv.append(escaparCsv(cita.getObservacion())).append("\n");
        });

        String nombreArchivo = "citas_medico_" + medicoId + "_" + fecha + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(csv.toString());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public co.edu.unicauca.piedraazul.agenda.application.dto.CitaResponse crearCita(
            @RequestBody CrearCitaCommand command
    ) {
        return crearCitaUseCase.crearCita(command);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> cambiarEstadoCita(
            @PathVariable Long id,
            @RequestBody CambiarEstadoCitaRequest request
    ) {
        if (request == null || request.getEstado() == null || request.getEstado().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nuevo estado de la cita es obligatorio."
            );
        }

        Cita cita = obtenerCitaPorId(id);

        EstadoCita nuevoEstado = convertirEstado(request.getEstado());

        validarCambioEstado(cita.getEstado(), nuevoEstado);

        cita.setEstado(nuevoEstado);

        if (request.getObservacion() != null && !request.getObservacion().trim().isEmpty()) {
            cita.setObservacion(request.getObservacion().trim());
        }

        Cita citaActualizada = citaRepository.save(cita);

        return ResponseEntity.ok(convertirCitaAMap(citaActualizada));
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Map<String, Object>> reagendarCita(
            @PathVariable Long id,
            @RequestBody ReagendarCitaRequest request
    ) {
        validarSolicitudReagendamiento(request);

        Cita cita = obtenerCitaPorId(id);

        validarCitaPuedeReagendarse(cita);

        LocalDate fechaAnterior = cita.getFecha();
        LocalTime horaAnterior = cita.getHora();

        LocalDate fechaNueva = request.getFechaNueva();
        LocalTime horaNueva = request.getHoraNueva();

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

        boolean horarioOcupado = citaRepository.existsByMedicoAndFechaAndHoraAndIdNot(
                cita.getMedico(),
                fechaNueva,
                horaNueva,
                cita.getId()
        );

        if (horarioOcupado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El médico/terapista ya tiene una cita registrada en la nueva fecha y hora seleccionada."
            );
        }

        HistorialReagendamiento historial = new HistorialReagendamiento();
        historial.setCita(cita);
        historial.setFechaAnterior(fechaAnterior);
        historial.setHoraAnterior(horaAnterior);
        historial.setFechaNueva(fechaNueva);
        historial.setHoraNueva(horaNueva);
        historial.setResponsable(normalizarTextoOpcional(request.getResponsable(), "Sistema"));
        historial.setMotivo(normalizarTextoOpcional(request.getMotivo(), "Re-agendamiento de cita"));
        historial.setFechaCambio(LocalDateTime.now());

        historialReagendamientoRepository.save(historial);

        cita.setFecha(fechaNueva);
        cita.setHora(horaNueva);

        String observacionActual = cita.getObservacion() == null ? "" : cita.getObservacion().trim();
        String nuevaObservacion = "Re-agendada de " + fechaAnterior + " " + horaAnterior
                + " a " + fechaNueva + " " + horaNueva
                + ". Responsable: " + historial.getResponsable()
                + ". Motivo: " + historial.getMotivo();

        if (observacionActual.isBlank()) {
            cita.setObservacion(nuevaObservacion);
        } else {
            cita.setObservacion(observacionActual + " | " + nuevaObservacion);
        }

        Cita citaActualizada = citaRepository.save(cita);

        Map<String, Object> response = convertirCitaAMap(citaActualizada);
        response.put("mensaje", "Cita re-agendada correctamente.");
        response.put("fechaAnterior", fechaAnterior);
        response.put("horaAnterior", horaAnterior);
        response.put("fechaNueva", fechaNueva);
        response.put("horaNueva", horaNueva);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/historial-reagendamientos")
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> listarHistorialReagendamientos(@PathVariable Long id) {
        obtenerCitaPorId(id);

        return historialReagendamientoRepository.findByCitaIdOrderByFechaCambioDesc(id)
                .stream()
                .map(this::convertirHistorialAMap)
                .toList();
    }

    private Cita obtenerCitaPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe una cita con id: " + id
                ));
    }

    private void validarSolicitudReagendamiento(ReagendarCitaRequest request) {
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

    private void validarCitaPuedeReagendarse(Cita cita) {
        if (cita.getEstado() == EstadoCita.ATENDIDA
                || cita.getEstado() == EstadoCita.CANCELADA
                || cita.getEstado() == EstadoCita.NO_VINO
                || cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La cita ya está en un estado final y no puede ser re-agendada."
            );
        }
    }

    private EstadoCita convertirEstado(String estado) {
        try {
            return EstadoCita.valueOf(estado.trim().toUpperCase());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado inválido. Valores permitidos: CONFIRMADA, ATENDIDA, CANCELADA, NO_VINO."
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
                || estadoActual == EstadoCita.CANCELADA
                || estadoActual == EstadoCita.NO_VINO
                || estadoActual == EstadoCita.COMPLETADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La cita ya está en un estado final y no puede ser modificada."
            );
        }

        if (nuevoEstado != EstadoCita.CONFIRMADA
                && nuevoEstado != EstadoCita.ATENDIDA
                && nuevoEstado != EstadoCita.CANCELADA
                && nuevoEstado != EstadoCita.NO_VINO
                && nuevoEstado != EstadoCita.COMPLETADA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado no permitido para cambio de cita."
            );
        }
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

    private Map<String, Object> convertirHistorialAMap(HistorialReagendamiento historial) {
        Map<String, Object> response = new HashMap<>();

        response.put("id", historial.getId());
        response.put("citaId", historial.getCita().getId());
        response.put("fechaAnterior", historial.getFechaAnterior());
        response.put("horaAnterior", historial.getHoraAnterior());
        response.put("fechaNueva", historial.getFechaNueva());
        response.put("horaNueva", historial.getHoraNueva());
        response.put("responsable", historial.getResponsable());
        response.put("motivo", historial.getMotivo());
        response.put("fechaCambio", historial.getFechaCambio());

        return response;
    }

    private String normalizarTextoOpcional(String valor, String valorPorDefecto) {
        if (valor == null || valor.trim().isEmpty()) {
            return valorPorDefecto;
        }

        return valor.trim().replaceAll("\\s+", " ");
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }

        String valorEscapado = valor.replace("\"", "\"\"");

        if (valorEscapado.contains(",") || valorEscapado.contains("\n") || valorEscapado.contains("\"")) {
            return "\"" + valorEscapado + "\"";
        }

        return valorEscapado;
    }

    public static class CambiarEstadoCitaRequest {

        private String estado;
        private String observacion;

        public CambiarEstadoCitaRequest() {
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public String getObservacion() {
            return observacion;
        }

        public void setObservacion(String observacion) {
            this.observacion = observacion;
        }
    }

    public static class ReagendarCitaRequest {

        private LocalDate fechaNueva;
        private LocalTime horaNueva;
        private String responsable;
        private String motivo;

        public ReagendarCitaRequest() {
        }

        public LocalDate getFechaNueva() {
            return fechaNueva;
        }

        public void setFechaNueva(LocalDate fechaNueva) {
            this.fechaNueva = fechaNueva;
        }

        public LocalTime getHoraNueva() {
            return horaNueva;
        }

        public void setHoraNueva(LocalTime horaNueva) {
            this.horaNueva = horaNueva;
        }

        public String getResponsable() {
            return responsable;
        }

        public void setResponsable(String responsable) {
            this.responsable = responsable;
        }

        public String getMotivo() {
            return motivo;
        }

        public void setMotivo(String motivo) {
            this.motivo = motivo;
        }
    }
}