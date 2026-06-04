package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarCitasPort;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.repository.CitaRepository;

@Component
public class BuscarCitasPersistenceAdapter implements BuscarCitasPort {

    private final CitaRepository citaRepository;

    public BuscarCitasPersistenceAdapter(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public Optional<Cita> buscarPorId(Long citaId) {
        return citaRepository.findById(citaId);
    }

    @Override
    public List<Cita> buscarPorMedicoYFecha(Medico medico, LocalDate fecha) {
        return citaRepository.findByMedicoAndFechaOrderByHoraAsc(medico, fecha);
    }

    @Override
    public List<Cita> buscarPorNumeroDocumentoPaciente(String numeroDocumento) {
        return citaRepository.findByPacienteNumeroDocumentoOrderByFechaAscHoraAsc(numeroDocumento);
    }

    @Override
    public boolean existeHorarioOcupadoDiferenteDeCita(Medico medico,
                                                        LocalDate fecha,
                                                        LocalTime hora,
                                                        Long citaId) {
        return citaRepository.existsByMedicoAndFechaAndHoraAndIdNot(
                medico,
                fecha,
                hora,
                citaId
        );
    }

    @Override
    public Cita guardar(Cita cita) {
        return citaRepository.save(cita);
    }
}