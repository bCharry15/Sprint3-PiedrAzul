package co.edu.unicauca.piedraazul.agenda.domain.port.in;

import java.util.Map;

import co.edu.unicauca.piedraazul.agenda.model.dto.CambiarEstadoCitaRequest;

public interface CambiarEstadoCitaUseCase {

    Map<String, Object> cambiarEstado(Long citaId, CambiarEstadoCitaRequest request);
}