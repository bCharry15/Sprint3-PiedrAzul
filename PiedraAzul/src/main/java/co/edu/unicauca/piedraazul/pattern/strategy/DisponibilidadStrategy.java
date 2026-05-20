package co.edu.unicauca.piedraazul.pattern.strategy;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public interface DisponibilidadStrategy {

    List<LocalTime> calcularFranjasDisponibles(
            LocalTime horaInicio,
            LocalTime horaFin,
            Integer intervaloMinutos,
            Set<LocalTime> horasOcupadas
    );
}