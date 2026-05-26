package co.edu.unicauca.piedraazul.agenda.domain.port.in;

import java.time.LocalDate;

import co.edu.unicauca.piedraazul.agenda.model.dto.CitasPorMedicoFechaResponse;

public interface ConsultarCitasPorMedicoFechaUseCase {

    CitasPorMedicoFechaResponse consultar(Long medicoId, LocalDate fecha);
}