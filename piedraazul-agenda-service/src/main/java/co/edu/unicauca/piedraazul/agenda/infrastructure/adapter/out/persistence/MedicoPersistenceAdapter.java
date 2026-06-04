package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarMedicoPort;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.repository.MedicoRepository;

@Component
public class MedicoPersistenceAdapter implements BuscarMedicoPort {

    private final MedicoRepository medicoRepository;

    public MedicoPersistenceAdapter(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @Override
    public Optional<Medico> buscarPorId(Long medicoId) {
        return medicoRepository.findByIdAndActivoTrue(medicoId);
    }
}