package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.GestionarMedicosPort;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.repository.MedicoRepository;

@Component
public class MedicoGestionPersistenceAdapter implements GestionarMedicosPort {

    private final MedicoRepository medicoRepository;

    public MedicoGestionPersistenceAdapter(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @Override
    public List<Medico> listarTodos() {
        return medicoRepository.findAll();
    }

    @Override
    public Optional<Medico> buscarPorId(Long medicoId) {
        return medicoRepository.findById(medicoId);
    }

    @Override
    public Medico guardar(Medico medico) {
        return medicoRepository.save(medico);
    }
}