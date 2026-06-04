package co.edu.unicauca.piedraazul.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import co.edu.unicauca.piedraazul.model.Cita;
import co.edu.unicauca.piedraazul.model.Medico;
import co.edu.unicauca.piedraazul.model.Paciente;
import co.edu.unicauca.piedraazul.model.dto.HistorialReagendamientoTablaModel;

public interface ICitaService {

    Cita crearCita(Paciente paciente,
                   Medico medico,
                   LocalDate fecha,
                   LocalTime hora,
                   String observacion);

    List<Cita> buscarPorMedicoYFecha(Medico medico, LocalDate fecha);

    long contarPorMedicoYFecha(Medico medico, LocalDate fecha);

    Cita cambiarEstadoCita(Long citaId, String estado, String observacion);

    List<HistorialReagendamientoTablaModel> consultarHistorialReagendamientos(Long citaId);
}