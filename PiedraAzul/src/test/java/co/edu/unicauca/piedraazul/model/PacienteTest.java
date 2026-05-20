package co.edu.unicauca.piedraazul.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PacienteTest {

    @Test
    void getNombreCompletoDebeConcatenarNombresYApellidos() {
        Paciente paciente = new Paciente();
        paciente.setNombres("Julian");
        paciente.setApellidos("Zuñiga");

        assertEquals("Julian Zuñiga", paciente.getNombreCompleto());
    }
}
