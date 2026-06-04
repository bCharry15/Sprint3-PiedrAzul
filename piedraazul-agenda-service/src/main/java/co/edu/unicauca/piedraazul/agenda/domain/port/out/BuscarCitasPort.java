package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;

public interface BuscarCitasPort {

    Optional<Cita> buscarPorId(Long citaId);

    List<Cita> buscarPorMedicoYFecha(Medico medico, LocalDate fecha);

    List<Cita> buscarPorNumeroDocumentoPaciente(String numeroDocumento);

    boolean existeHorarioOcupadoDiferenteDeCita(Medico medico,
                                                 LocalDate fecha,
                                                 LocalTime hora,
                                                 Long citaId);

    Cita guardar(Cita cita);
}