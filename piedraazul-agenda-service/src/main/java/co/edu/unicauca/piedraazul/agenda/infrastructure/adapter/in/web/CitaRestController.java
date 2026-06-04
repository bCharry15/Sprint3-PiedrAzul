package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.web;

import java.time.LocalDate;
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

import co.edu.unicauca.piedraazul.agenda.application.dto.CrearCitaCommand;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.CambiarEstadoCitaUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.ConsultarCitasPacienteUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.ConsultarCitasPorMedicoFechaUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.ConsultarHistorialReagendamientoUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.CrearCitaUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.ReagendarCitaUseCase;
import co.edu.unicauca.piedraazul.agenda.model.dto.CambiarEstadoCitaRequest;
import co.edu.unicauca.piedraazul.agenda.model.dto.CitaResponse;
import co.edu.unicauca.piedraazul.agenda.model.dto.CitasPorMedicoFechaResponse;
import co.edu.unicauca.piedraazul.agenda.model.dto.ReagendarCitaRequest;

@RestController
@RequestMapping("/api/citas")
public class CitaRestController {

    private final ConsultarCitasPorMedicoFechaUseCase consultarCitasPorMedicoFechaUseCase;
    private final CrearCitaUseCase crearCitaUseCase;
    private final ConsultarCitasPacienteUseCase consultarCitasPacienteUseCase;
    private final CambiarEstadoCitaUseCase cambiarEstadoCitaUseCase;
    private final ReagendarCitaUseCase reagendarCitaUseCase;
    private final ConsultarHistorialReagendamientoUseCase consultarHistorialReagendamientoUseCase;

    public CitaRestController(ConsultarCitasPorMedicoFechaUseCase consultarCitasPorMedicoFechaUseCase,
                              CrearCitaUseCase crearCitaUseCase,
                              ConsultarCitasPacienteUseCase consultarCitasPacienteUseCase,
                              CambiarEstadoCitaUseCase cambiarEstadoCitaUseCase,
                              ReagendarCitaUseCase reagendarCitaUseCase,
                              ConsultarHistorialReagendamientoUseCase consultarHistorialReagendamientoUseCase) {
        this.consultarCitasPorMedicoFechaUseCase = consultarCitasPorMedicoFechaUseCase;
        this.crearCitaUseCase = crearCitaUseCase;
        this.consultarCitasPacienteUseCase = consultarCitasPacienteUseCase;
        this.cambiarEstadoCitaUseCase = cambiarEstadoCitaUseCase;
        this.reagendarCitaUseCase = reagendarCitaUseCase;
        this.consultarHistorialReagendamientoUseCase = consultarHistorialReagendamientoUseCase;
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
        return ResponseEntity.ok(
                cambiarEstadoCitaUseCase.cambiarEstado(id, request)
        );
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Map<String, Object>> reagendarCita(
            @PathVariable Long id,
            @RequestBody ReagendarCitaRequest request
    ) {
        return ResponseEntity.ok(
                reagendarCitaUseCase.reagendar(id, request)
        );
    }

    @GetMapping("/{id}/historial-reagendamientos")
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> listarHistorialReagendamientos(@PathVariable Long id) {
        return consultarHistorialReagendamientoUseCase.consultarPorCitaId(id);
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
}