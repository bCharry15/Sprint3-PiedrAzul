package co.edu.unicauca.piedraazul.service;

import co.edu.unicauca.piedraazul.model.Cita;
import co.edu.unicauca.piedraazul.model.Medico;
import co.edu.unicauca.piedraazul.model.Paciente;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ICitaService {

    /**
     * Crea y persiste una nueva cita médica.
     * Lanza IllegalArgumentException si ya existe una cita para ese médico,
     * fecha y hora.
     */
    Cita crearCita(Paciente paciente, Medico medico, LocalDate fecha,
                   LocalTime hora, String observacion);

    /**
     * Retorna las citas de un médico en una fecha específica, ordenadas por hora.
     */
    List<Cita> buscarPorMedicoYFecha(Medico medico, LocalDate fecha);

    /**
     * Retorna la cantidad de citas de un médico en una fecha específica.
     */
    long contarPorMedicoYFecha(Medico medico, LocalDate fecha);
}
