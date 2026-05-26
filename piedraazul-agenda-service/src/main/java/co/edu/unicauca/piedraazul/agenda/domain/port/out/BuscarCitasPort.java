package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.time.LocalDate;
import java.util.List;

import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;

public interface BuscarCitasPort {

    List<Cita> buscarPorMedicoYFecha(Medico medico, LocalDate fecha);

    List<Cita> buscarPorNumeroDocumentoPaciente(String numeroDocumento);
}