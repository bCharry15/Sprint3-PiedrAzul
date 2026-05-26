package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.persistence;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.ObtenerOCrearPacientePort;
import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import co.edu.unicauca.piedraazul.agenda.model.enums.Genero;
import co.edu.unicauca.piedraazul.agenda.repository.PacienteRepository;

@Component
public class PacientePersistenceAdapter implements ObtenerOCrearPacientePort {

    private final PacienteRepository pacienteRepository;

    public PacientePersistenceAdapter(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public Paciente obtenerOCrearPaciente(
            String numeroDocumento,
            String tipoDocumento,
            String nombres,
            String apellidos,
            String celular,
            Genero genero,
            LocalDate fechaNacimiento,
            String correo
    ) {
        return pacienteRepository.findByNumeroDocumento(numeroDocumento)
                .orElseGet(() -> crearPaciente(
                        numeroDocumento,
                        tipoDocumento,
                        nombres,
                        apellidos,
                        celular,
                        genero,
                        fechaNacimiento,
                        correo
                ));
    }

    private Paciente crearPaciente(
            String numeroDocumento,
            String tipoDocumento,
            String nombres,
            String apellidos,
            String celular,
            Genero genero,
            LocalDate fechaNacimiento,
            String correo
    ) {
        Paciente paciente = new Paciente();
        paciente.setNumeroDocumento(numeroDocumento);
        paciente.setTipoDocumento(tipoDocumento);
        paciente.setNombres(nombres);
        paciente.setApellidos(apellidos);
        paciente.setCelular(celular);
        paciente.setGenero(genero);
        paciente.setFechaNacimiento(fechaNacimiento);
        paciente.setCorreo(correo);

        return pacienteRepository.save(paciente);
    }
}