package co.edu.unicauca.piedraazul.agenda.controller;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.dto.CrearDisponibilidadRequest;
import co.edu.unicauca.piedraazul.agenda.model.dto.DisponibilidadMedicoResponse;
import co.edu.unicauca.piedraazul.agenda.repository.DisponibilidadMedicoRepository;
import co.edu.unicauca.piedraazul.agenda.service.IMedicoService;

@RestController
@RequestMapping("/api/configuraciones-disponibilidad")
public class ConfiguracionDisponibilidadRestController {

    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;
    private final IMedicoService medicoService;

    public ConfiguracionDisponibilidadRestController(
            DisponibilidadMedicoRepository disponibilidadMedicoRepository,
            IMedicoService medicoService
    ) {
        this.disponibilidadMedicoRepository = disponibilidadMedicoRepository;
        this.medicoService = medicoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DisponibilidadMedicoResponse crearConfiguracion(@RequestBody CrearDisponibilidadRequest request) {
        validarRequest(request);

        Medico medico = medicoService.buscarPorId(request.getMedicoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista con id: " + request.getMedicoId()
                ));

        DisponibilidadMedico disponibilidad = new DisponibilidadMedico();
        disponibilidad.setMedico(medico);
        disponibilidad.setDiaSemana(convertirDiaSemana(request.getDiaSemana()));
        disponibilidad.setHoraInicio(request.getHoraInicio());
        disponibilidad.setHoraFin(request.getHoraFin());
        disponibilidad.setIntervaloMinutos(request.getIntervaloMinutos());
        disponibilidad.setVentanaSemanas(request.getVentanaSemanas());
        disponibilidad.setActivo(request.getActivo() == null || request.getActivo());

        DisponibilidadMedico guardada = disponibilidadMedicoRepository.save(disponibilidad);

        return convertirAResponse(guardada);
    }

    @GetMapping("/medico/{medicoId}")
    @ResponseStatus(HttpStatus.OK)
    public List<DisponibilidadMedicoResponse> listarPorMedico(@PathVariable Long medicoId) {
        Medico medico = medicoService.buscarPorId(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista con id: " + medicoId
                ));

        return disponibilidadMedicoRepository.findByMedicoAndActivoTrue(medico)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    private void validarRequest(CrearDisponibilidadRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud no puede estar vacía.");
        }

        if (request.getMedicoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El médico/terapista es obligatorio.");
        }

        if (request.getDiaSemana() == null || request.getDiaSemana().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El día de la semana es obligatorio.");
        }

        if (request.getHoraInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de inicio es obligatoria.");
        }

        if (request.getHoraFin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de fin es obligatoria.");
        }

        if (!request.getHoraInicio().isBefore(request.getHoraFin())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de inicio debe ser menor que la hora de fin.");
        }

        if (request.getIntervaloMinutos() == null || request.getIntervaloMinutos() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El intervalo debe ser mayor a cero.");
        }

        if (request.getVentanaSemanas() == null || request.getVentanaSemanas() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ventana de semanas debe ser mayor a cero.");
        }
    }

    private DayOfWeek convertirDiaSemana(String diaSemana) {
        try {
            return DayOfWeek.valueOf(diaSemana.trim().toUpperCase());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Día de la semana inválido. Usa MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY o SUNDAY."
            );
        }
    }

    private DisponibilidadMedicoResponse convertirAResponse(DisponibilidadMedico disponibilidad) {
        return new DisponibilidadMedicoResponse(
                disponibilidad.getId(),
                disponibilidad.getMedico().getId(),
                disponibilidad.getMedico().getNombreCompleto(),
                disponibilidad.getDiaSemana().name(),
                disponibilidad.getHoraInicio(),
                disponibilidad.getHoraFin(),
                disponibilidad.getIntervaloMinutos(),
                disponibilidad.getVentanaSemanas(),
                disponibilidad.getActivo()
        );
    }
}




