package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.ConfigurarDisponibilidadPort;
import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.repository.DisponibilidadMedicoRepository;

@Component
public class ConfiguracionDisponibilidadPersistenceAdapter implements ConfigurarDisponibilidadPort {

    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;

    public ConfiguracionDisponibilidadPersistenceAdapter(DisponibilidadMedicoRepository disponibilidadMedicoRepository) {
        this.disponibilidadMedicoRepository = disponibilidadMedicoRepository;
    }

    @Override
    public DisponibilidadMedico guardar(DisponibilidadMedico disponibilidad) {
        return disponibilidadMedicoRepository.save(disponibilidad);
    }

    @Override
    public Optional<DisponibilidadMedico> buscarPorId(Long disponibilidadId) {
        return disponibilidadMedicoRepository.findById(disponibilidadId);
    }

    @Override
    public List<DisponibilidadMedico> buscarPorMedicoActivo(Medico medico) {
        return disponibilidadMedicoRepository.findByMedicoAndActivoTrue(medico);
    }
}