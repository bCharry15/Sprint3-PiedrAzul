package co.edu.unicauca.piedraazul.agenda.pattern.strategy;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class DisponibilidadPorIntervaloStrategy implements DisponibilidadStrategy {

    @Override
    public List<LocalTime> calcularFranjasDisponibles(
            LocalTime horaInicio,
            LocalTime horaFin,
            Integer intervaloMinutos,
            Set<LocalTime> horasOcupadas
    ) {
        List<LocalTime> franjasDisponibles = new ArrayList<>();

        LocalTime horaActual = horaInicio;

        while (horaActual.isBefore(horaFin)) {
            LocalTime horaNormalizada = horaActual.truncatedTo(ChronoUnit.SECONDS);

            if (!horasOcupadas.contains(horaNormalizada)) {
                franjasDisponibles.add(horaNormalizada);
            }

            horaActual = horaActual.plusMinutes(intervaloMinutos);
        }

        return franjasDisponibles;
    }
}

