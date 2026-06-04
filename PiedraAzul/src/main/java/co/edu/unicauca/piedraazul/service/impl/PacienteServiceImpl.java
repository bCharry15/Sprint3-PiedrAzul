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

        try {
            return agendaServiceClient.buscarPacientePorNumeroDocumento(numeroDocumento.trim());

        } catch (Exception e) {
            System.out.println("PACIENTE-SERVICE -> Paciente no encontrado por documento: " + numeroDocumento);
            return null;
        }
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

        Paciente paciente = construirPaciente(
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

        Paciente pacienteExistente = buscarPorNumeroDocumento(numeroDocumento);

        if (pacienteExistente != null) {
            return pacienteExistente;
        }

        /*
         * En el panel agendador no necesariamente existe un username,
         * porque el agendador puede crear una cita para un paciente que no tiene cuenta.
         * Por eso aquí solo se construye el objeto paciente.
         * El backend lo crea realmente al enviar la cita por /api/citas.
         */
        return construirPaciente(
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

    private Paciente construirPaciente(String username,
                                       String numeroDocumento,
                                       String tipoDocumento,
                                       String nombres,
                                       String apellidos,
                                       String celular,
                                       Genero genero,
                                       LocalDate fechaNacimiento,
                                       String correo) {

        Paciente paciente = new Paciente();

        paciente.setUsername(limpiarTextoONull(username));
        paciente.setNumeroDocumento(limpiarTexto(numeroDocumento));
        paciente.setTipoDocumento(limpiarTexto(tipoDocumento));
        paciente.setNombres(limpiarTexto(nombres));
        paciente.setApellidos(limpiarTexto(apellidos));
        paciente.setCelular(limpiarTexto(celular));
        paciente.setGenero(genero != null ? genero : Genero.OTRO);
        paciente.setFechaNacimiento(fechaNacimiento);
        paciente.setCorreo(limpiarTextoONull(correo));

        return paciente;
    }

    private String limpiarTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String limpiarTextoONull(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        return valor.trim();
    }
}