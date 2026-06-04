package co.edu.unicauca.piedraazul.agenda.domain.port.in;

import java.util.Map;

import co.edu.unicauca.piedraazul.agenda.model.dto.ReagendarCitaRequest;

public interface ReagendarCitaUseCase {

    Map<String, Object> reagendar(Long citaId, ReagendarCitaRequest request);
}