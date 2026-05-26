package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.time.LocalDate;
import java.time.LocalTime;

import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.Paciente;

public interface CrearCitaPort {

    Cita crearCita(
            Paciente paciente,
            Medico medico,
            LocalDate fecha,
            LocalTime hora,
            String observacion
    );
}