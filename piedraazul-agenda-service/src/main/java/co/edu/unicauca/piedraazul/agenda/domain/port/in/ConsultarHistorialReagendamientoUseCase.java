package co.edu.unicauca.piedraazul.agenda.domain.port.in;

import java.util.List;
import java.util.Map;

public interface ConsultarHistorialReagendamientoUseCase {

    List<Map<String, Object>> consultarPorCitaId(Long citaId);
}