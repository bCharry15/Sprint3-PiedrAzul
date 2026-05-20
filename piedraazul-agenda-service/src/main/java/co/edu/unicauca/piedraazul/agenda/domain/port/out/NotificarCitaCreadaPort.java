package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.time.LocalDate;
import java.time.LocalTime;

public interface NotificarCitaCreadaPort {

    void notificarCitaCreada(
            Long citaId,
            String nombrePaciente,
            String correoPaciente,
            String celularPaciente,
            String nombreMedico,
            LocalDate fecha,
            LocalTime hora
    );
}