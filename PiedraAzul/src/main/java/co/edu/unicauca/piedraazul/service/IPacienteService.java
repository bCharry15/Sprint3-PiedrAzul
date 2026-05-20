package co.edu.unicauca.piedraazul.service;

import java.time.LocalDate;

import co.edu.unicauca.piedraazul.model.Paciente;
import co.edu.unicauca.piedraazul.model.enums.Genero;

public interface IPacienteService {

    Paciente buscarPorNumeroDocumento(String numeroDocumento);

    Paciente buscarPorUsername(String username);

    Paciente obtenerOCrearPaciente(String username,
                                   String numeroDocumento,
                                   String tipoDocumento,
                                   String nombres,
                                   String apellidos,
                                   String celular,
                                   Genero genero,
                                   LocalDate fechaNacimiento,
                                   String correo);

    Paciente obtenerOCrearPaciente(String numeroDocumento,
                                   String tipoDocumento,
                                   String nombres,
                                   String apellidos,
                                   String celular,
                                   Genero genero,
                                   LocalDate fechaNacimiento,
                                   String correo);
}