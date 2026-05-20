package co.edu.unicauca.piedraazul.notification.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias - CitaCreadaNotificationRequest DTO")
class CitaCreadaNotificationRequestTest {

    private CitaCreadaNotificationRequest request;

    @BeforeEach
    void setUp() {
        request = new CitaCreadaNotificationRequest();
        request.setCitaId(5L);
        request.setPaciente("Carlos Gómez");
        request.setMedico("Dra. Vargas");
        request.setFecha("2026-07-10");
        request.setHora("10:30");
        request.setCorreoPaciente("carlos@correo.com");
        request.setCelularPaciente("3109876543");
    }

    @Test
    @DisplayName("El citaId se almacena correctamente")
    void citaIdCorrecto() {
        assertEquals(5L, request.getCitaId());
    }

    @Test
    @DisplayName("El paciente se almacena correctamente")
    void pacienteCorrecto() {
        assertEquals("Carlos Gómez", request.getPaciente());
    }

    @Test
    @DisplayName("El médico se almacena correctamente")
    void medicoCorrecto() {
        assertEquals("Dra. Vargas", request.getMedico());
    }

    @Test
    @DisplayName("La fecha como string se almacena correctamente")
    void fechaCorrecta() {
        assertEquals("2026-07-10", request.getFecha());
    }

    @Test
    @DisplayName("La hora como string se almacena correctamente")
    void horaCorrecta() {
        assertEquals("10:30", request.getHora());
    }

    @Test
    @DisplayName("El correo del paciente se almacena correctamente")
    void correoCorecto() {
        assertEquals("carlos@correo.com", request.getCorreoPaciente());
    }

    @Test
    @DisplayName("El celular del paciente se almacena correctamente")
    void celularCorrecto() {
        assertEquals("3109876543", request.getCelularPaciente());
    }

    @Test
    @DisplayName("El correo puede ser nulo (paciente sin correo)")
    void correoPuedeSerNulo() {
        request.setCorreoPaciente(null);
        assertNull(request.getCorreoPaciente());
    }

    @Test
    @DisplayName("El DTO se puede crear con constructor vacío y asignar campos uno a uno")
    void constructorVacioYSetters() {
        CitaCreadaNotificationRequest nuevo = new CitaCreadaNotificationRequest();
        nuevo.setCitaId(99L);
        nuevo.setPaciente("Test");
        assertEquals(99L, nuevo.getCitaId());
        assertEquals("Test", nuevo.getPaciente());
    }
}
