package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import co.edu.unicauca.piedraazul.agenda.model.HistorialReagendamiento;

public interface GuardarHistorialReagendamientoPort {

    HistorialReagendamiento guardar(HistorialReagendamiento historial);
}