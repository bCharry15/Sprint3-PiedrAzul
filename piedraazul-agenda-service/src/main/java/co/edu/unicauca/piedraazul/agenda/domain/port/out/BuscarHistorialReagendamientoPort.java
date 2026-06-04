package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.util.List;

import co.edu.unicauca.piedraazul.agenda.model.HistorialReagendamiento;

public interface BuscarHistorialReagendamientoPort {

    List<HistorialReagendamiento> buscarPorCitaId(Long citaId);
}