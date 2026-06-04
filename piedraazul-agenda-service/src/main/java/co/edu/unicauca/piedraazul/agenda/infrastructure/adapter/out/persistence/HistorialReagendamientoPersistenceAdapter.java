package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarHistorialReagendamientoPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.GuardarHistorialReagendamientoPort;
import co.edu.unicauca.piedraazul.agenda.model.HistorialReagendamiento;
import co.edu.unicauca.piedraazul.agenda.repository.HistorialReagendamientoRepository;

@Component
public class HistorialReagendamientoPersistenceAdapter implements GuardarHistorialReagendamientoPort,
        BuscarHistorialReagendamientoPort {

    private final HistorialReagendamientoRepository historialReagendamientoRepository;

    public HistorialReagendamientoPersistenceAdapter(
            HistorialReagendamientoRepository historialReagendamientoRepository) {
        this.historialReagendamientoRepository = historialReagendamientoRepository;
    }

    @Override
    public HistorialReagendamiento guardar(HistorialReagendamiento historial) {
        return historialReagendamientoRepository.save(historial);
    }

    @Override
    public List<HistorialReagendamiento> buscarPorCitaId(Long citaId) {
        return historialReagendamientoRepository.findByCitaIdOrderByFechaCambioDesc(citaId);
    }
}