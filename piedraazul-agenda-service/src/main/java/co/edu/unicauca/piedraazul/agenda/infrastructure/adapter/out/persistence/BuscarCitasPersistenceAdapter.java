package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;

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
    public List<Cita> buscarPorMedicoYFecha(Medico medico, LocalDate fecha) {
        return citaRepository.findByMedicoAndFechaOrderByHoraAsc(medico, fecha);
    }

    @Override
    public List<Cita> buscarPorNumeroDocumentoPaciente(String numeroDocumento) {
        return citaRepository.findByPacienteNumeroDocumentoOrderByFechaAscHoraAsc(numeroDocumento);
    }
}