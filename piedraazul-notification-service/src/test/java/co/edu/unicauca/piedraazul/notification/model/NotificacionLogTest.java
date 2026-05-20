package co.edu.unicauca.piedraazul.notification.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias - NotificacionLog")
class NotificacionLogTest {

    private NotificacionLog log;

    @BeforeEach
    void setUp() {
        log = new NotificacionLog();
        log.setCitaId(10L);
        log.setPaciente("María Rodríguez");
        log.setMedico("Dr. López");
        log.setCorreoPaciente("maria@correo.com");
        log.setCelularPaciente("3001234567");
        log.setEstado("ENVIADO");
        log.setFechaEnvio(LocalDateTime.of(2026, 6, 15, 9, 30));
    }

    @Test
    @DisplayName("El citaId se almacena correctamente")
    void citaIdAlmacenadoCorrectamente() {
        assertEquals(10L, log.getCitaId());
    }

    @Test
    @DisplayName("El nombre del paciente se almacena correctamente")
    void pacienteAlmacenadoCorrectamente() {
        assertEquals("María Rodríguez", log.getPaciente());
    }

    @Test
    @DisplayName("El nombre del médico se almacena correctamente")
    void medicoAlmacenadoCorrectamente() {
        assertEquals("Dr. López", log.getMedico());
    }

    @Test
    @DisplayName("El correo del paciente se almacena correctamente")
    void correoAlmacenadoCorrectamente() {
        assertEquals("maria@correo.com", log.getCorreoPaciente());
    }

    @Test
    @DisplayName("El celular del paciente se almacena correctamente")
    void celularAlmacenadoCorrectamente() {
        assertEquals("3001234567", log.getCelularPaciente());
    }

    @Test
    @DisplayName("El estado ENVIADO se almacena correctamente")
    void estadoEnviadoAlmacenado() {
        assertEquals("ENVIADO", log.getEstado());
    }

    @Test
    @DisplayName("El estado puede cambiar a FALLIDO")
    void estadoPuedeCambiarAFallido() {
        log.setEstado("FALLIDO");
        assertEquals("FALLIDO", log.getEstado());
    }

    @Test
    @DisplayName("La fecha de envío se almacena correctamente")
    void fechaEnvioAlmacenadaCorrectamente() {
        assertEquals(LocalDateTime.of(2026, 6, 15, 9, 30), log.getFechaEnvio());
    }

    @Test
    @DisplayName("El correo puede ser nulo (paciente sin correo)")
    void correoPuedeSerNulo() {
        log.setCorreoPaciente(null);
        assertNull(log.getCorreoPaciente());
    }

    @Test
    @DisplayName("El ID es nulo antes de persistirse en BD")
    void idEsNuloAntesDeGuardar() {
        NotificacionLog nuevo = new NotificacionLog();
        assertNull(nuevo.getId());
    }
}
