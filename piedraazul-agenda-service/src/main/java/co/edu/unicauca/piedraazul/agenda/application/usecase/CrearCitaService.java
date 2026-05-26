package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.application.dto.CitaResponse;
import co.edu.unicauca.piedraazul.agenda.application.dto.CrearCitaCommand;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.CrearCitaUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarCitasPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarDisponibilidadMedicoPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarMedicoPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.CrearCitaPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.ObtenerOCrearPacientePort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.PublicarCitaCreadaEventPort;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;
import co.edu.unicauca.piedraazul.agenda.model.enums.Genero;

@Service
public class CrearCitaService implements CrearCitaUseCase {

    private static final int VENTANA_SEMANAS_DEFAULT = 4;

    private static final Set<LocalDate> FESTIVOS_COLOMBIA_2026 = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 12),
            LocalDate.of(2026, 3, 23),
            LocalDate.of(2026, 4, 2),
            LocalDate.of(2026, 4, 3),
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 18),
            LocalDate.of(2026, 6, 8),
            LocalDate.of(2026, 6, 15),
            LocalDate.of(2026, 6, 29),
            LocalDate.of(2026, 7, 20),
            LocalDate.of(2026, 8, 7),
            LocalDate.of(2026, 8, 17),
            LocalDate.of(2026, 10, 12),
            LocalDate.of(2026, 11, 2),
            LocalDate.of(2026, 11, 16),
            LocalDate.of(2026, 12, 8),
            LocalDate.of(2026, 12, 25)
    );

    private final BuscarMedicoPort buscarMedicoPort;
    private final BuscarDisponibilidadMedicoPort buscarDisponibilidadMedicoPort;
    private final ObtenerOCrearPacientePort obtenerOCrearPacientePort;
    private final CrearCitaPort crearCitaPort;
    private final BuscarCitasPort buscarCitasPort;
    private final PublicarCitaCreadaEventPort publicarCitaCreadaEventPort;
    private final DbiiSincronizacionService dbiiSincronizacionService;

    public CrearCitaService(
            BuscarMedicoPort buscarMedicoPort,
            BuscarDisponibilidadMedicoPort buscarDisponibilidadMedicoPort,
            ObtenerOCrearPacientePort obtenerOCrearPacientePort,
            CrearCitaPort crearCitaPort,
            BuscarCitasPort buscarCitasPort,
            PublicarCitaCreadaEventPort publicarCitaCreadaEventPort,
            DbiiSincronizacionService dbiiSincronizacionService
    ) {
        this.buscarMedicoPort = buscarMedicoPort;
        this.buscarDisponibilidadMedicoPort = buscarDisponibilidadMedicoPort;
        this.obtenerOCrearPacientePort = obtenerOCrearPacientePort;
        this.crearCitaPort = crearCitaPort;
        this.buscarCitasPort = buscarCitasPort;
        this.publicarCitaCreadaEventPort = publicarCitaCreadaEventPort;
        this.dbiiSincronizacionService = dbiiSincronizacionService;
    }

    @Override
    public CitaResponse crearCita(CrearCitaCommand command) {
        validarSolicitudCrearCita(command);
        validarFechaNoFestiva(command.getFecha());

        Medico medico = buscarMedicoPort.buscarPorId(command.getMedicoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un medico/terapista con id: " + command.getMedicoId()
                ));

        validarDisponibilidadConfigurada(medico, command);
        validarPrimeraCitaConsultaGeneral(command, medico);

        Genero genero = convertirGenero(command.getGenero());

        Paciente paciente = obtenerOCrearPacientePort.obtenerOCrearPaciente(
                normalizarTextoSimple(command.getNumeroDocumento()),
                normalizarTipoDocumento(command.getTipoDocumento()),
                normalizarNombreOApellido(command.getNombres()),
                normalizarNombreOApellido(command.getApellidos()),
                normalizarTextoSimple(command.getCelular()),
                genero,
                command.getFechaNacimiento(),
                normalizarCorreo(command.getCorreo())
        );

        validarPacienteSinCitaActiva(paciente);

        try {
            Cita citaCreada = crearCitaPort.crearCita(
                    paciente,
                    medico,
                    command.getFecha(),
                    command.getHora(),
                    normalizarTextoOpcional(command.getObservacion())
            );

            dbiiSincronizacionService.sincronizarPacienteYCita(citaCreada);

            publicarEventoCitaCreada(citaCreada);

            return new CitaResponse(
                    citaCreada.getId(),
                    citaCreada.getPaciente().getNumeroDocumento(),
                    citaCreada.getPaciente().getNombreCompleto(),
                    citaCreada.getMedico().getNombreCompleto(),
                    citaCreada.getFecha(),
                    citaCreada.getHora(),
                    citaCreada.getEstado().name()
            );

        } catch (ResponseStatusException ex) {
            throw ex;

        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo crear la cita. Detalle: " + ex.getMessage()
            );
        }
    }

    private void publicarEventoCitaCreada(Cita citaCreada) {
        try {
            publicarCitaCreadaEventPort.publicar(citaCreada);
        } catch (Exception e) {
            System.err.println("AGENDA-SERVICE -> No se pudo publicar evento de cita creada: " + e.getMessage());
        }
    }

    private void validarSolicitudCrearCita(CrearCitaCommand command) {
        if (command == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La solicitud no puede estar vacia."
            );
        }

        validarTextoObligatorio(command.getNumeroDocumento(), "El numero de documento es obligatorio.");
        validarTextoObligatorio(command.getTipoDocumento(), "El tipo de documento es obligatorio.");
        validarTextoObligatorio(command.getNombres(), "Los nombres son obligatorios.");
        validarTextoObligatorio(command.getApellidos(), "Los apellidos son obligatorios.");
        validarTextoObligatorio(command.getCelular(), "El celular es obligatorio.");
        validarTextoObligatorio(command.getGenero(), "El genero es obligatorio.");

        if (command.getMedicoId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El medico/terapista es obligatorio."
            );
        }

        if (command.getFecha() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de la cita es obligatoria."
            );
        }

        if (command.getHora() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La hora de la cita es obligatoria."
            );
        }
    }

    private void validarTextoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    private void validarFechaNoFestiva(LocalDate fecha) {
        if (fecha == null) {
            return;
        }

        if (FESTIVOS_COLOMBIA_2026.contains(fecha)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede agendar una cita en dia festivo en Colombia."
            );
        }
    }

    private void validarDisponibilidadConfigurada(Medico medico, CrearCitaCommand command) {
        DayOfWeek diaSemana = command.getFecha().getDayOfWeek();

        DisponibilidadMedico disponibilidad = buscarDisponibilidadMedicoPort
                .buscarDisponibilidadActiva(medico, diaSemana)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El medico/terapista no tiene disponibilidad configurada para el dia seleccionado."
                ));

        validarFechaDentroDeVentana(command.getFecha(), disponibilidad);
        validarHoraDentroDelHorario(command, disponibilidad);
        validarHoraEnIntervalo(command, disponibilidad);
    }

    private void validarFechaDentroDeVentana(LocalDate fecha, DisponibilidadMedico disponibilidad) {
        LocalDate fechaActual = LocalDate.now();

        int ventanaSemanas = disponibilidad.getVentanaSemanas() != null && disponibilidad.getVentanaSemanas() > 0
                ? disponibilidad.getVentanaSemanas()
                : VENTANA_SEMANAS_DEFAULT;

        LocalDate fechaLimite = fechaActual.plusWeeks(ventanaSemanas);

        if (fecha.isBefore(fechaActual)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede crear una cita en una fecha pasada."
            );
        }

        if (fecha.isAfter(fechaLimite)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha supera la ventana de agendamiento permitida de "
                            + ventanaSemanas + " semanas."
            );
        }
    }

    private void validarHoraDentroDelHorario(CrearCitaCommand command, DisponibilidadMedico disponibilidad) {
        if (disponibilidad.getHoraInicio() == null || disponibilidad.getHoraFin() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La disponibilidad del medico/terapista esta incompleta."
            );
        }

        if (command.getHora().isBefore(disponibilidad.getHoraInicio())
                || !command.getHora().isBefore(disponibilidad.getHoraFin())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La hora seleccionada esta fuera de la franja configurada para el medico/terapista."
            );
        }
    }

    private void validarHoraEnIntervalo(CrearCitaCommand command, DisponibilidadMedico disponibilidad) {
        if (disponibilidad.getIntervaloMinutos() == null || disponibilidad.getIntervaloMinutos() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El intervalo de atencion del medico/terapista no esta configurado correctamente."
            );
        }

        long minutosDesdeInicio = ChronoUnit.MINUTES.between(
                disponibilidad.getHoraInicio(),
                command.getHora()
        );

        if (minutosDesdeInicio < 0 || minutosDesdeInicio % disponibilidad.getIntervaloMinutos() != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La hora seleccionada no coincide con el intervalo configurado de "
                            + disponibilidad.getIntervaloMinutos() + " minutos."
            );
        }
    }

    private void validarPacienteSinCitaActiva(Paciente paciente) {
        if (paciente == null || paciente.getNumeroDocumento() == null) {
            return;
        }

        List<Cita> citasPaciente = buscarCitasPort.buscarPorNumeroDocumentoPaciente(
                paciente.getNumeroDocumento()
        );

        boolean tieneCitaActiva = citasPaciente.stream()
                .anyMatch(cita -> esEstadoActivo(cita.getEstado()));

        if (tieneCitaActiva) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El paciente ya tiene una cita agendada o pendiente. "
                            + "No puede crear una nueva cita hasta que la cita actual sea atendida, cancelada o marcada como no asistida."
            );
        }
    }

    private boolean esEstadoActivo(EstadoCita estado) {
        if (estado == null) {
            return false;
        }

        String nombreEstado = estado.name();

        return "PROGRAMADA".equalsIgnoreCase(nombreEstado)
                || "CONFIRMADA".equalsIgnoreCase(nombreEstado)
                || "PENDIENTE".equalsIgnoreCase(nombreEstado);
    }

    private void validarPrimeraCitaConsultaGeneral(CrearCitaCommand command, Medico medicoSeleccionado) {
        if (command == null || command.getNumeroDocumento() == null || medicoSeleccionado == null) {
            return;
        }

        List<Cita> citasPaciente = buscarCitasPort.buscarPorNumeroDocumentoPaciente(
                normalizarTextoSimple(command.getNumeroDocumento())
        );

        boolean tieneCitaAtendidaConsultaGeneral = citasPaciente.stream()
                .anyMatch(cita -> cita.getEstado() == EstadoCita.ATENDIDA
                        && cita.getMedico() != null
                        && esConsultaGeneral(cita.getMedico()));

        if (!tieneCitaAtendidaConsultaGeneral && !esConsultaGeneral(medicoSeleccionado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El paciente debe tener primero una cita de Consulta General atendida antes de agendar terapia neural, quiropraxia, fisioterapia u otra especialidad."
            );
        }
    }

    private boolean esConsultaGeneral(Medico medico) {
        if (medico == null || medico.getEspecialidad() == null) {
            return false;
        }

        String especialidad = removerTildes(medico.getEspecialidad())
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", " ");

        return especialidad.equals("medicina general")
                || especialidad.equals("consulta general")
                || especialidad.contains("medicina general")
                || especialidad.contains("consulta general");
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
                    "Genero invalido. Valores permitidos: HOMBRE, MUJER, OTRO."
            );
        }
    }

    private String normalizarNombreOApellido(String valor) {
        if (valor == null) {
            return "";
        }

        String sinTildes = removerTildes(valor);
        String limpio = sinTildes.trim().replaceAll("\\s+", " ");

        if (limpio.isEmpty()) {
            return "";
        }

        String[] palabras = limpio.toLowerCase().split(" ");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (palabra.isBlank()) {
                continue;
            }

            resultado.append(Character.toUpperCase(palabra.charAt(0)));

            if (palabra.length() > 1) {
                resultado.append(palabra.substring(1));
            }

            resultado.append(" ");
        }

        return resultado.toString().trim();
    }

    private String normalizarTextoSimple(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.trim().replaceAll("\\s+", " ");
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        return valor.trim().replaceAll("\\s+", " ");
    }

    private String normalizarCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }

        return correo.trim().toLowerCase();
    }

    private String removerTildes(String valor) {
        if (valor == null) {
            return "";
        }

        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}