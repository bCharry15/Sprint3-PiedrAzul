package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.GuardarCitaPort;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.repository.CitaRepository;

@Component
public class CitaPersistenceAdapter implements GuardarCitaPort {

    private final CitaRepository citaRepository;

    public CitaPersistenceAdapter(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public Cita guardar(Cita cita) {
        return citaRepository.save(cita);
    }
}