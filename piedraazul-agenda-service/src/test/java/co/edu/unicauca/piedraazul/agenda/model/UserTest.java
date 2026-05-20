package co.edu.unicauca.piedraazul.agenda.model;

import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.agenda.observer.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias - Modelo User (con Observer)")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("agendador01");
        user.setPassword("pass_encriptado");
        user.setRole(UserRole.AGENDADOR);
        user.setStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("El username se almacena correctamente")
    void usernameCorrecto() {
        assertEquals("agendador01", user.getUsername());
    }

    @Test
    @DisplayName("El rol AGENDADOR se asigna correctamente")
    void rolAgendadorAsignado() {
        assertEquals(UserRole.AGENDADOR, user.getRole());
    }

    @Test
    @DisplayName("El rol MEDICO se puede asignar")
    void rolMedicoAsignado() {
        user.setRole(UserRole.MEDICO);
        assertEquals(UserRole.MEDICO, user.getRole());
    }

    @Test
    @DisplayName("El rol ADMIN se puede asignar")
    void rolAdminAsignado() {
        user.setRole(UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    @DisplayName("El estado ACTIVE se almacena correctamente")
    void estadoActiveCorrecto() {
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    @DisplayName("El estado INACTIVE se puede asignar")
    void estadoInactiveAsignado() {
        user.setStatus(UserStatus.INACTIVE);
        assertEquals(UserStatus.INACTIVE, user.getStatus());
    }

    @Test
    @DisplayName("Al cambiar el estado, los observers registrados son notificados")
    void observerEsNotificadoAlCambiarEstado() {
        List<String> mensajesRecibidos = new ArrayList<>();

        Observer observadorDePrueba = mensajesRecibidos::add;
        user.attach(observadorDePrueba);

        user.setStatus(UserStatus.INACTIVE);

        assertEquals(1, mensajesRecibidos.size());
        assertTrue(mensajesRecibidos.get(0).contains("agendador01"));
        assertTrue(mensajesRecibidos.get(0).contains("INACTIVE"));
    }

    @Test
    @DisplayName("Al detach de un observer, ya no recibe notificaciones")
    void observerDetachadoNoRecibeNotificaciones() {
        List<String> mensajesRecibidos = new ArrayList<>();
        Observer observadorDePrueba = mensajesRecibidos::add;

        user.attach(observadorDePrueba);
        user.detach(observadorDePrueba);

        user.setStatus(UserStatus.INACTIVE);

        assertTrue(mensajesRecibidos.isEmpty());
    }

    @Test
    @DisplayName("Múltiples observers reciben la notificación al cambiar estado")
    void multiplesObserversRecibidos() {
        List<String> mensajes1 = new ArrayList<>();
        List<String> mensajes2 = new ArrayList<>();

        user.attach(mensajes1::add);
        user.attach(mensajes2::add);

        user.setStatus(UserStatus.INACTIVE);

        assertEquals(1, mensajes1.size());
        assertEquals(1, mensajes2.size());
    }

    @Test
    @DisplayName("notifyObservers envía el mensaje correcto a todos los observers")
    void notifyObserversEnviaMensajeCorrecto() {
        List<String> mensajes = new ArrayList<>();
        user.attach(mensajes::add);

        user.notifyObservers("Mensaje de prueba manual");

        assertEquals(1, mensajes.size());
        assertEquals("Mensaje de prueba manual", mensajes.get(0));
    }
}
