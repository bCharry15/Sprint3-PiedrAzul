package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.ConsultarDisponibilidadUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarCitasPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarDisponibilidadMedicoPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarMedicoPort;
import co.edu.unicauca.piedraazul.agenda.domain.service.disponibilidad.DisponibilidadStrategy;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.dto.DisponibilidadResponse;

@Service
public class ConsultarDisponibilidadService implements ConsultarDisponibilidadUseCase {

    private final BuscarMedicoPort buscarMedicoPort;
    private final BuscarDisponibilidadMedicoPort buscarDisponibilidadMedicoPort;
    private final BuscarCitasPort buscarCitasPort;
    private final DisponibilidadStrategy disponibilidadStrategy;

    public ConsultarDisponibilidadService(BuscarMedicoPort buscarMedicoPort,
                                           BuscarDisponibilidadMedicoPort buscarDisponibilidadMedicoPort,
                                           BuscarCitasPort buscarCitasPort,
                                           DisponibilidadStrategy disponibilidadStrategy) {
        this.buscarMedicoPort = buscarMedicoPort;
        this.buscarDisponibilidadMedicoPort = buscarDisponibilidadMedicoPort;
        this.buscarCitasPort = buscarCitasPort;
        this.disponibilidadStrategy = disponibilidadStrategy;
    }

    @Override
    public DisponibilidadResponse consultar(Long medicoId, LocalDate fecha) {
        Medico medico = buscarMedicoPort.buscarPorId(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista con id: " + medicoId
                ));

        DayOfWeek diaSemana = fecha.getDayOfWeek();

        DisponibilidadMedico disponibilidad = buscarDisponibilidadMedicoPort
                .buscarDisponibilidadActiva(medico, diaSemana)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El médico/terapista no tiene disponibilidad configurada para el día: " + diaSemana
                ));

        validarVentanaDeAgendamiento(disponibilidad, fecha);

        List<Cita> citasDelDia = buscarCitasPort.buscarPorMedicoYFecha(medico, fecha);

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

    private void validarVentanaDeAgendamiento(DisponibilidadMedico disponibilidad, LocalDate fecha) {
        Integer ventanaSemanas = disponibilidad.getVentanaSemanas();

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