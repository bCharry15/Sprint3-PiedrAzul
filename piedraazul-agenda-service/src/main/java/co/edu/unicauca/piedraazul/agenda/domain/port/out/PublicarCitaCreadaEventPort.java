package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import co.edu.unicauca.piedraazul.agenda.model.Cita;

public interface PublicarCitaCreadaEventPort {

    void publicar(Cita citaCreada);
}