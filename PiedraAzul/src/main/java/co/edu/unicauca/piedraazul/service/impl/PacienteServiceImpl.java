package co.edu.unicauca.piedraazul.service.impl;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.model.Paciente;
import co.edu.unicauca.piedraazul.model.enums.Genero;
import co.edu.unicauca.piedraazul.service.IPacienteService;

@Service
public class PacienteServiceImpl implements IPacienteService {

    private final Map<String, Paciente> pacientesPorDocumento = new ConcurrentHashMap<>();
    private final Map<String, Paciente> pacientesPorUsername = new ConcurrentHashMap<>();

    @Override
    public Paciente buscarPorNumeroDocumento(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return null;
        }

        return pacientesPorDocumento.get(numeroDocumento.trim());
    }

    @Override
    public Paciente buscarPorUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        return pacientesPorUsername.get(username.trim());
    }

    @Override
    public Paciente obtenerOCrearPaciente(String username,
                                          String numeroDocumento,
                                          String tipoDocumento,
                                          String nombres,
                                          String apellidos,
                                          String celular,
                                          Genero genero,
                                          LocalDate fechaNacimiento,
                                          String correo) {

        username = limpiarTexto(username);
        numeroDocumento = limpiarTexto(numeroDocumento);

        Paciente paciente = null;

        if (!username.isEmpty()) {
            paciente = pacientesPorUsername.get(username);
        }

        if (paciente == null && !numeroDocumento.isEmpty()) {
            paciente = pacientesPorDocumento.get(numeroDocumento);
        }

        if (paciente == null) {
            paciente = new Paciente();
        }

        paciente.setUsername(username);
        paciente.setNumeroDocumento(numeroDocumento);
        paciente.setTipoDocumento(limpiarTexto(tipoDocumento));
        paciente.setNombres(limpiarTexto(nombres));
        paciente.setApellidos(limpiarTexto(apellidos));
        paciente.setCelular(limpiarTexto(celular));
        paciente.setGenero(genero != null ? genero : Genero.OTRO);
        paciente.setFechaNacimiento(fechaNacimiento);
        paciente.setCorreo(limpiarTexto(correo));

        if (!numeroDocumento.isEmpty()) {
            pacientesPorDocumento.put(numeroDocumento, paciente);
        }

        if (!username.isEmpty()) {
            pacientesPorUsername.put(username, paciente);
        }

        return paciente;
    }

    @Override
    public Paciente obtenerOCrearPaciente(String numeroDocumento,
                                          String tipoDocumento,
                                          String nombres,
                                          String apellidos,
                                          String celular,
                                          Genero genero,
                                          LocalDate fechaNacimiento,
                                          String correo) {

        return obtenerOCrearPaciente(
                null,
                numeroDocumento,
                tipoDocumento,
                nombres,
                apellidos,
                celular,
                genero,
                fechaNacimiento,
                correo
        );
    }

    private String limpiarTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}