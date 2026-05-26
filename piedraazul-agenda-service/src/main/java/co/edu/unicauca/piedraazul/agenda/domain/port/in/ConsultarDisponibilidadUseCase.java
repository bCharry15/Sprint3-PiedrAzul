package co.edu.unicauca.piedraazul.agenda.domain.port.in;

import java.time.LocalDate;

import co.edu.unicauca.piedraazul.agenda.model.dto.DisponibilidadResponse;

public interface ConsultarDisponibilidadUseCase {

    DisponibilidadResponse consultar(Long medicoId, LocalDate fecha);
}