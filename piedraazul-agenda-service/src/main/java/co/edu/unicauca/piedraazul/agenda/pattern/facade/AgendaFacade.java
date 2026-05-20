package co.edu.unicauca.piedraazul.agenda.pattern.facade;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.event.CitaCreadaEvent;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import co.edu.unicauca.piedraazul.agenda.model.dto.CrearCitaRequest;
import co.edu.unicauca.piedraazul.agenda.model.enums.Genero;
import co.edu.unicauca.piedraazul.agenda.repository.DisponibilidadMedicoRepository;
import co.edu.unicauca.piedraazul.agenda.service.ICitaService;
import co.edu.unicauca.piedraazul.agenda.service.IMedicoService;
import co.edu.unicauca.piedraazul.agenda.service.IPacienteService;

@Component
public class AgendaFacade {

    private final ICitaService citaService;
    private final IMedicoService medicoService;
    private final IPacienteService pacienteService;
    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AgendaFacade(ICitaService citaService,
                        IMedicoService medicoService,
                        IPacienteService pacienteService,
                        DisponibilidadMedicoRepository disponibilidadMedicoRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.citaService = citaService;
        this.medicoService = medicoService;
        this.pacienteService = pacienteService;
        this.disponibilidadMedicoRepository = disponibilidadMedicoRepository;
        this.eventPublisher = eventPublisher;
    }

    public Cita crearCitaDesdeSolicitud(CrearCitaRequest request) {
        validarSolicitudCrearCita(request);

        Medico medico = medicoService.buscarPorId(request.getMedicoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista con id: " + request.getMedicoId()
                ));

        validarDisponibilidadConfigurada(medico, request);

        Genero genero = convertirGenero(request.getGenero());

        Paciente paciente = pacienteService.obtenerOCrearPaciente(
                request.getNumeroDocumento(),
                normalizarTipoDocumento(request.getTipoDocumento()),
                request.getNombres(),
                request.getApellidos(),
                request.getCelular(),
                genero,
                request.getFechaNacimiento(),
                request.getCorreo()
        );

        try {
            Cita citaCreada = citaService.crearCita(
                    paciente,
                    medico,
                    request.getFecha(),
                    request.getHora(),
                    request.getObservacion()
            );

            publicarEventoCitaCreada(citaCreada);

            return citaCreada;

        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
    }

    private void publicarEventoCitaCreada(Cita citaCreada) {
        Paciente paciente = citaCreada.getPaciente();
        Medico medico = citaCreada.getMedico();

        eventPublisher.publishEvent(new CitaCreadaEvent(
                citaCreada.getId(),
                paciente.getId(),
                paciente.getNombreCompleto(),
                paciente.getCorreo(),
                paciente.getCelular(),
                medico.getId(),
                medico.getNombreCompleto(),
                citaCreada.getFecha(),
                citaCreada.getHora()
        ));
    }

    private void validarDisponibilidadConfigurada(Medico medico, CrearCitaRequest request) {
        DayOfWeek diaSemana = request.getFecha().getDayOfWeek();

        DisponibilidadMedico disponibilidad = disponibilidadMedicoRepository
                .findFirstByMedicoAndDiaSemanaAndActivoTrueOrderByIdDesc(medico, diaSemana)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El médico/terapista no tiene disponibilidad configurada para el día: " + diaSemana
                ));

        LocalDate fechaActual = LocalDate.now();
        LocalDate fechaLimite = fechaActual.plusWeeks(disponibilidad.getVentanaSemanas());

        if (request.getFecha().isBefore(fechaActual)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede crear una cita en una fecha pasada."
            );
        }

        if (request.getFecha().isAfter(fechaLimite)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha supera la ventana de agendamiento permitida de "
                            + disponibilidad.getVentanaSemanas() + " semanas."
            );
        }

        if (request.getHora().isBefore(disponibilidad.getHoraInicio())
                || !request.getHora().isBefore(disponibilidad.getHoraFin())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La hora seleccionada está fuera de la franja configurada para el médico/terapista."
            );
        }

        long minutosDesdeInicio = ChronoUnit.MINUTES.between(
                disponibilidad.getHoraInicio(),
                request.getHora()
        );

        if (minutosDesdeInicio % disponibilidad.getIntervaloMinutos() != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La hora seleccionada no coincide con el intervalo configurado de "
                            + disponibilidad.getIntervaloMinutos() + " minutos."
            );
        }
    }

    private void validarSolicitudCrearCita(CrearCitaRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud no puede estar vacía.");
        }

        validarTextoObligatorio(request.getNumeroDocumento(), "El número de documento es obligatorio.");
        validarTextoObligatorio(request.getNombres(), "Los nombres son obligatorios.");
        validarTextoObligatorio(request.getApellidos(), "Los apellidos son obligatorios.");
        validarTextoObligatorio(request.getCelular(), "El celular es obligatorio.");
        validarTextoObligatorio(request.getGenero(), "El género es obligatorio.");

        if (request.getMedicoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El médico/terapista es obligatorio.");
        }

        if (request.getFecha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de la cita es obligatoria.");
        }

        if (request.getHora() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de la cita es obligatoria.");
        }
    }

    private void validarTextoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    private String normalizarTipoDocumento(String tipoDocumento) {
        if (tipoDocumento == null || tipoDocumento.trim().isEmpty()) {
            return "CC";
        }

        return tipoDocumento.trim().toUpperCase();
    }

    private Genero convertirGenero(String genero) {
        try {
            return Genero.valueOf(genero.trim().toUpperCase());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Género inválido. Valores permitidos: HOMBRE, MUJER, OTRO."
            );
        }
    }
}