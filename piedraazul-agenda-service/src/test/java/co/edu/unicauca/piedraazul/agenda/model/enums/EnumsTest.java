package co.edu.unicauca.piedraazul.agenda.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias - Enums del dominio")
class EnumsTest {

    // ─── EstadoCita ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("EstadoCita contiene exactamente los 3 estados esperados")
    void estadoCita_tieneLosTresEstados() {
        EstadoCita[] valores = EstadoCita.values();
        assertEquals(3, valores.length);
    }

    @Test
    @DisplayName("EstadoCita.PROGRAMADA existe y es accesible por nombre")
    void estadoCita_programadaExiste() {
        assertEquals(EstadoCita.PROGRAMADA, EstadoCita.valueOf("PROGRAMADA"));
    }

    @Test
    @DisplayName("EstadoCita.COMPLETADA existe y es accesible por nombre")
    void estadoCita_completadaExiste() {
        assertEquals(EstadoCita.COMPLETADA, EstadoCita.valueOf("COMPLETADA"));
    }

    @Test
    @DisplayName("EstadoCita.CANCELADA existe y es accesible por nombre")
    void estadoCita_canceladaExiste() {
        assertEquals(EstadoCita.CANCELADA, EstadoCita.valueOf("CANCELADA"));
    }

    @Test
    @DisplayName("EstadoCita.valueOf lanza excepción para valores desconocidos")
    void estadoCita_valorInvalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> EstadoCita.valueOf("PENDIENTE"));
    }

    // ─── Genero ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Genero contiene exactamente los 3 valores esperados")
    void genero_tieneLosTresValores() {
        assertEquals(3, Genero.values().length);
    }

    @Test
    @DisplayName("Genero.HOMBRE existe")
    void genero_hombreExiste() {
        assertEquals(Genero.HOMBRE, Genero.valueOf("HOMBRE"));
    }

    @Test
    @DisplayName("Genero.MUJER existe")
    void genero_mujerExiste() {
        assertEquals(Genero.MUJER, Genero.valueOf("MUJER"));
    }

    @Test
    @DisplayName("Genero.OTRO existe")
    void genero_otroExiste() {
        assertEquals(Genero.OTRO, Genero.valueOf("OTRO"));
    }

    // ─── UserRole ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UserRole contiene exactamente los 4 roles esperados")
    void userRole_tieneCuatroRoles() {
        assertEquals(4, UserRole.values().length);
    }

    @Test
    @DisplayName("UserRole.ADMIN existe")
    void userRole_adminExiste() {
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
    }

    @Test
    @DisplayName("UserRole.AGENDADOR existe")
    void userRole_agendadorExiste() {
        assertEquals(UserRole.AGENDADOR, UserRole.valueOf("AGENDADOR"));
    }

    @Test
    @DisplayName("UserRole.MEDICO existe")
    void userRole_medicoExiste() {
        assertEquals(UserRole.MEDICO, UserRole.valueOf("MEDICO"));
    }

    @Test
    @DisplayName("UserRole.PACIENTE existe")
    void userRole_pacienteExiste() {
        assertEquals(UserRole.PACIENTE, UserRole.valueOf("PACIENTE"));
    }

    // ─── UserStatus ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("UserStatus contiene exactamente los 2 estados esperados")
    void userStatus_tieneDoEstados() {
        assertEquals(2, UserStatus.values().length);
    }

    @Test
    @DisplayName("UserStatus.ACTIVE existe")
    void userStatus_activeExiste() {
        assertEquals(UserStatus.ACTIVE, UserStatus.valueOf("ACTIVE"));
    }

    @Test
    @DisplayName("UserStatus.INACTIVE existe")
    void userStatus_inactiveExiste() {
        assertEquals(UserStatus.INACTIVE, UserStatus.valueOf("INACTIVE"));
    }
}
