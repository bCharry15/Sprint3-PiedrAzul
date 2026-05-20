package co.edu.unicauca.piedraazul.agenda.service.impl;

import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import co.edu.unicauca.piedraazul.agenda.repository.CitaRepository;
import co.edu.unicauca.piedraazul.agenda.service.ICitaService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CitaServiceImpl implements ICitaService {

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public Cita crearCita(Paciente paciente, Medico medico, LocalDate fecha,
                          LocalTime hora, String observacion) {
        boolean yaExiste = citaRepository
                .existsByMedicoAndFechaAndHora(medico, fecha, hora);

        if (yaExiste) {
            throw new IllegalArgumentException(
                    "Ya existe una cita para ese médico en la fecha y hora seleccionadas.");
        }

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setObservacion(observacion);

        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> buscarPorMedicoYFecha(Medico medico, LocalDate fecha) {
        return citaRepository.findByMedicoAndFechaOrderByHoraAsc(medico, fecha);
    }

    @Override
    public long contarPorMedicoYFecha(Medico medico, LocalDate fecha) {
        return buscarPorMedicoYFecha(medico, fecha).size();
    }
}

