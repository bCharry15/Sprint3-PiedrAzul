package co.edu.unicauca.piedraazul.agenda.model;

import co.edu.unicauca.piedraazul.agenda.model.enums.Genero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias - Modelo Paciente")
class PacienteTest {

    private Paciente paciente;

    @BeforeEach
    void setUp() {
        paciente = new Paciente();
        paciente.setNumeroDocumento("987654321");
        paciente.setTipoDocumento("CC");
        paciente.setNombres("María");
        paciente.setApellidos("Rodríguez");
        paciente.setCelular("3001234567");
        paciente.setGenero(Genero.MUJER);
        paciente.setFechaNacimiento(LocalDate.of(1990, 3, 22));
        paciente.setCorreo("maria.rodriguez@correo.com");
    }

    @Test
    @DisplayName("El nombre completo concatena nombres y apellidos correctamente")
    void nombreCompletoEsCorrecto() {
        assertEquals("María Rodríguez", paciente.getNombreCompleto());
    }

    @Test
    @DisplayName("El número de documento se almacena correctamente")
    void numeroDocumentoCorrecto() {
        assertEquals("987654321", paciente.getNumeroDocumento());
    }

    @Test
    @DisplayName("El tipo de documento se almacena correctamente")
    void tipoDocumentoCorrecto() {
        assertEquals("CC", paciente.getTipoDocumento());
    }

    @Test
    @DisplayName("El celular se almacena correctamente")
    void celularCorrecto() {
        assertEquals("3001234567", paciente.getCelular());
    }

    @Test
    @DisplayName("El género se almacena correctamente")
    void generoCorrecto() {
        assertEquals(Genero.MUJER, paciente.getGenero());
    }

    @Test
    @DisplayName("La fecha de nacimiento se almacena correctamente")
    void fechaNacimientoCorrecto() {
        assertEquals(LocalDate.of(1990, 3, 22), paciente.getFechaNacimiento());
    }

    @Test
    @DisplayName("El correo se almacena correctamente")
    void correoCorrecto() {
        assertEquals("maria.rodriguez@correo.com", paciente.getCorreo());
    }

    @Test
    @DisplayName("El nombre completo con nombres compuestos funciona correctamente")
    void nombreCompletoConNombresCompuestos() {
        paciente.setNombres("Ana María");
        paciente.setApellidos("López Castro");
        assertEquals("Ana María López Castro", paciente.getNombreCompleto());
    }

    @Test
    @DisplayName("Paciente puede tener género HOMBRE")
    void pacienteConGeneroHombre() {
        paciente.setGenero(Genero.HOMBRE);
        assertEquals(Genero.HOMBRE, paciente.getGenero());
    }

    @Test
    @DisplayName("Paciente puede tener género OTRO")
    void pacienteConGeneroOtro() {
        paciente.setGenero(Genero.OTRO);
        assertEquals(Genero.OTRO, paciente.getGenero());
    }

    @Test
    @DisplayName("El correo puede ser nulo (campo opcional)")
    void correoPuedeSerNulo() {
        paciente.setCorreo(null);
        assertNull(paciente.getCorreo());
    }
}
