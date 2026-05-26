package co.edu.unicauca.piedraazul.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.Paciente;
import co.edu.unicauca.piedraazul.model.enums.Genero;
import co.edu.unicauca.piedraazul.service.IPacienteService;

@Service
public class PacienteServiceImpl implements IPacienteService {

    private final AgendaServiceClient agendaServiceClient;

    public PacienteServiceImpl(AgendaServiceClient agendaServiceClient) {
        this.agendaServiceClient = agendaServiceClient;
    }

    @Override
    public Paciente buscarPorNumeroDocumento(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return null;
        }

        /*
         * En el flujo actual el perfil se consulta principalmente por username.
         * La búsqueda por documento se conserva para compatibilidad con código anterior.
         */
        return null;
    }

    @Override
    public Paciente buscarPorUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        try {
            return agendaServiceClient.buscarPerfilPacientePorUsername(username.trim());
        } catch (Exception e) {
            System.out.println("PACIENTE-SERVICE -> Perfil no encontrado para username: " + username);
            return null;
        }
    }

    @Override
    public Paciente guardarPerfil(String username,
                                  String numeroDocumento,
                                  String tipoDocumento,
                                  String nombres,
                                  String apellidos,
                                  String celular,
                                  Genero genero,
                                  LocalDate fechaNacimiento,
                                  String correo) {

        Paciente paciente = new Paciente();
        paciente.setUsername(limpiarTexto(username));
        paciente.setNumeroDocumento(limpiarTexto(numeroDocumento));
        paciente.setTipoDocumento(limpiarTexto(tipoDocumento));
        paciente.setNombres(limpiarTexto(nombres));
        paciente.setApellidos(limpiarTexto(apellidos));
        paciente.setCelular(limpiarTexto(celular));
        paciente.setGenero(genero != null ? genero : Genero.OTRO);
        paciente.setFechaNacimiento(fechaNacimiento);
        paciente.setCorreo(limpiarTexto(correo));

        return agendaServiceClient.guardarPerfilPaciente(paciente);
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

        return guardarPerfil(
                username,
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

    @Override
    public Paciente obtenerOCrearPaciente(String numeroDocumento,
                                          String tipoDocumento,
                                          String nombres,
                                          String apellidos,
                                          String celular,
                                          Genero genero,
                                          LocalDate fechaNacimiento,
                                          String correo) {

        return guardarPerfil(
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