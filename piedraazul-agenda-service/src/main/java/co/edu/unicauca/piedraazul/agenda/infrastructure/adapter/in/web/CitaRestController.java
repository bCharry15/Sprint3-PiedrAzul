package co.edu.unicauca.piedraazul.agenda.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.application.dto.CrearCitaCommand;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.CrearCitaUseCase;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.dto.CitaResponse;
import co.edu.unicauca.piedraazul.agenda.model.dto.CitasPorMedicoFechaResponse;
import co.edu.unicauca.piedraazul.agenda.pattern.builder.CitaResponseBuilder;
import co.edu.unicauca.piedraazul.agenda.service.ICitaService;
import co.edu.unicauca.piedraazul.agenda.service.IMedicoService;

@RestController
@RequestMapping("/api/citas")
public class CitaRestController {

    private final ICitaService citaService;
    private final IMedicoService medicoService;
    private final CrearCitaUseCase crearCitaUseCase;

    public CitaRestController(ICitaService citaService,
                              IMedicoService medicoService,
                              CrearCitaUseCase crearCitaUseCase) {
        this.citaService = citaService;
        this.medicoService = medicoService;
        this.crearCitaUseCase = crearCitaUseCase;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CitasPorMedicoFechaResponse listarPorMedicoYFecha(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        Medico medico = obtenerMedicoPorId(medicoId);

        List<Cita> citas = citaService.buscarPorMedicoYFecha(medico, fecha);

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

    @GetMapping(value = "/exportar", produces = "text/csv")
    public ResponseEntity<String> exportarCitasPorMedicoYFecha(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        CitasPorMedicoFechaResponse response = listarPorMedicoYFecha(medicoId, fecha);

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

    private Medico obtenerMedicoPorId(Long medicoId) {
        return medicoService.buscarPorId(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista con id: " + medicoId
                ));
    }

    private CitaResponse convertirACitaResponse(Cita cita) {
        return CitaResponseBuilder.builder()
                .id(cita.getId())
                .pacienteId(cita.getPaciente().getId())
                .paciente(cita.getPaciente().getNombreCompleto())
                .medicoId(cita.getMedico().getId())
                .medico(cita.getMedico().getNombreCompleto())
                .fecha(cita.getFecha())
                .hora(cita.getHora())
                .estado(cita.getEstado().name())
                .observacion(cita.getObservacion())
                .build();
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