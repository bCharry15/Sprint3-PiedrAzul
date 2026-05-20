package co.edu.unicauca.piedraazul.agenda.model;

import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias - Modelo Medico")
class MedicoTest {

    private Medico medico;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("dr.lopez");
        user.setPassword("hashedpassword");
        user.setRole(UserRole.MEDICO);
        user.setStatus(UserStatus.ACTIVE);

        medico = new Medico();
        medico.setNombreCompleto("Dr. Juan López");
        medico.setEspecialidad("Neurología");
        medico.setIntervaloMinutos(45);
        medico.setUser(user);
    }

    @Test
    @DisplayName("El toString del médico retorna nombre y especialidad")
    void toStringRetornaNombreYEspecialidad() {
        String resultado = medico.toString();
        assertEquals("Dr. Juan López - Neurología", resultado);
    }

    @Test
    @DisplayName("El nombre completo del médico se almacena correctamente")
    void nombreCompletoAlmacenado() {
        assertEquals("Dr. Juan López", medico.getNombreCompleto());
    }

    @Test
    @DisplayName("La especialidad del médico se almacena correctamente")
    void especialidadAlmacenada() {
        assertEquals("Neurología", medico.getEspecialidad());
    }

    @Test
    @DisplayName("El intervalo de minutos se almacena correctamente")
    void intervaloMinutosAlmacenado() {
        assertEquals(45, medico.getIntervaloMinutos());
    }

    @Test
    @DisplayName("El usuario asociado al médico se almacena correctamente")
    void usuarioAsociadoAlmacenado() {
        assertNotNull(medico.getUser());
        assertEquals("dr.lopez", medico.getUser().getUsername());
    }

    @Test
    @DisplayName("La especialidad puede actualizarse")
    void actualizarEspecialidad() {
        medico.setEspecialidad("Cardiología");
        assertEquals("Cardiología", medico.getEspecialidad());
    }

    @Test
    @DisplayName("El intervalo de minutos puede actualizarse")
    void actualizarIntervaloMinutos() {
        medico.setIntervaloMinutos(30);
        assertEquals(30, medico.getIntervaloMinutos());
    }

    @Test
    @DisplayName("El médico puede no tener usuario asociado (nulo)")
    void medicoSinUsuario() {
        medico.setUser(null);
        assertNull(medico.getUser());
    }
}
