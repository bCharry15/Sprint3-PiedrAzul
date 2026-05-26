package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.CrearCitaPort;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;
import co.edu.unicauca.piedraazul.agenda.repository.CitaRepository;

@Component
public class CrearCitaPersistenceAdapter implements CrearCitaPort {

    private final CitaRepository citaRepository;

    public CrearCitaPersistenceAdapter(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public Cita crearCita(
            Paciente paciente,
            Medico medico,
            LocalDate fecha,
            LocalTime hora,
            String observacion
    ) {
        boolean yaExisteCita = citaRepository.existsByMedicoAndFechaAndHora(
                medico,
                fecha,
                hora
        );

        if (yaExisteCita) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una cita para el médico/terapista en la fecha y hora seleccionadas."
            );
        }

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setObservacion(observacion);
        cita.setEstado(EstadoCita.PROGRAMADA);

        return citaRepository.save(cita);
    }
}