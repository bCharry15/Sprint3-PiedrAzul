package co.edu.unicauca.piedraazul.agenda.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.dto.DisponibilidadResponse;
import co.edu.unicauca.piedraazul.agenda.pattern.strategy.DisponibilidadStrategy;
import co.edu.unicauca.piedraazul.agenda.repository.DisponibilidadMedicoRepository;
import co.edu.unicauca.piedraazul.agenda.service.ICitaService;
import co.edu.unicauca.piedraazul.agenda.service.IMedicoService;

@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadRestController {

    private final IMedicoService medicoService;
    private final ICitaService citaService;
    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;
    private final DisponibilidadStrategy disponibilidadStrategy;

    public DisponibilidadRestController(IMedicoService medicoService,
                                    ICitaService citaService,
                                    DisponibilidadMedicoRepository disponibilidadMedicoRepository,
                                    DisponibilidadStrategy disponibilidadStrategy) {
    this.medicoService = medicoService;
    this.citaService = citaService;
    this.disponibilidadMedicoRepository = disponibilidadMedicoRepository;
    this.disponibilidadStrategy = disponibilidadStrategy;
}

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public DisponibilidadResponse consultarDisponibilidad(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        Medico medico = medicoService.buscarPorId(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista con id: " + medicoId
                ));

        validarVentanaDeAgendamiento(medico, fecha);

        DayOfWeek diaSemana = fecha.getDayOfWeek();

        DisponibilidadMedico disponibilidad = disponibilidadMedicoRepository
                .findFirstByMedicoAndDiaSemanaAndActivoTrueOrderByIdDesc(medico, diaSemana)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El médico/terapista no tiene disponibilidad configurada para el día: " + diaSemana
                ));

        List<Cita> citasDelDia = citaService.buscarPorMedicoYFecha(medico, fecha);

        Set<LocalTime> horasOcupadas = new HashSet<>();

        for (Cita cita : citasDelDia) {
            horasOcupadas.add(cita.getHora());
        }

        List<LocalTime> franjasDisponibles = disponibilidadStrategy.calcularFranjasDisponibles(
        disponibilidad.getHoraInicio(),
        disponibilidad.getHoraFin(),
        disponibilidad.getIntervaloMinutos(),
        horasOcupadas
);

        return new DisponibilidadResponse(
                medico.getId(),
                medico.getNombreCompleto(),
                fecha,
                disponibilidad.getIntervaloMinutos(),
                disponibilidad.getHoraInicio(),
                disponibilidad.getHoraFin(),
                franjasDisponibles
        );
    }

    private void validarVentanaDeAgendamiento(Medico medico, LocalDate fecha) {
        List<DisponibilidadMedico> configuraciones = disponibilidadMedicoRepository.findByMedicoAndActivoTrue(medico);

        if (configuraciones.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El médico/terapista no tiene configuraciones de disponibilidad activas."
            );
        }

        Integer ventanaSemanas = configuraciones.get(0).getVentanaSemanas();

        if (ventanaSemanas == null || ventanaSemanas <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La ventana de agendamiento configurada no es válida."
            );
        }

        LocalDate fechaActual = LocalDate.now();
        LocalDate fechaLimite = fechaActual.plusWeeks(ventanaSemanas);

        if (fecha.isBefore(fechaActual)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede consultar disponibilidad para fechas pasadas."
            );
        }

        if (fecha.isAfter(fechaLimite)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha supera la ventana de agendamiento permitida de " + ventanaSemanas + " semanas."
            );
        }
    }
}




