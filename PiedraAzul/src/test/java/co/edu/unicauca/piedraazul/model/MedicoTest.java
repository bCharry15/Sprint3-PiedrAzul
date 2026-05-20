package co.edu.unicauca.piedraazul.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MedicoTest {

    @Test
    void toStringDebeMostrarNombreYEspecialidad() {
        Medico medico = new Medico();
        medico.setNombreCompleto("Ana Torres");
        medico.setEspecialidad("Pediatría");

        assertEquals("Ana Torres - Pediatría", medico.toString());
    }
}
