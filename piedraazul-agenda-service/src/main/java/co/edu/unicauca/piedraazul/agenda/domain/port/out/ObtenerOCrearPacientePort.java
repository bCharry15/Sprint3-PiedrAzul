package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.time.LocalDate;

import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import co.edu.unicauca.piedraazul.agenda.model.enums.Genero;

public interface ObtenerOCrearPacientePort {

    Paciente obtenerOCrearPaciente(
            String numeroDocumento,
            String tipoDocumento,
            String nombres,
            String apellidos,
            String celular,
            Genero genero,
            LocalDate fechaNacimiento,
            String correo
    );
}