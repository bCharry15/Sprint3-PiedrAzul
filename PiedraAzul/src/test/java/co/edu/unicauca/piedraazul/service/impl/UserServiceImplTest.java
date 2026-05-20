package co.edu.unicauca.piedraazul.service.impl;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.enums.UserRole;
import co.edu.unicauca.piedraazul.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.observer.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias - UserServiceImpl")
class UserServiceImplTest {

    private UserServiceImpl userService;
    private User user;
    private List<String> notificaciones;
    private Observer testObserver;

    // Fake (Stub) en lugar de Mockito para evitar el problema de Java 25 / InlineByteCode
    private boolean simularExitoRegistro = true;
    private boolean simularExitoLogin = true;
    private boolean simularExcepcion = false;

    @BeforeEach
    void setUp() {
        AgendaServiceClient fakeClient = new AgendaServiceClient() {
            @Override
            public void registrarUsuario(String username, String password, String role) {
                if (simularExcepcion) {
                    throw new RuntimeException("Error HTTP simulado");
                }
            }

            @Override
            public Map<String, String> login(String username, String password) {
                if (simularExcepcion) {
                    throw new RuntimeException("HTTP 401 Unauthorized");
                }
                if (!simularExitoLogin) {
                    return null;
                }
                Map<String, String> map = new HashMap<>();
                map.put("username", username);
                map.put("role", "PACIENTE");
                map.put("status", "ACTIVE");
                return map;
            }
        };

        userService = new UserServiceImpl(fakeClient);

        user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setRole(UserRole.PACIENTE);

        notificaciones = new ArrayList<>();
        testObserver = notificaciones::add;
    }

    @Test
    @DisplayName("registerUser: se registra correctamente e invoca el cliente Feign/HTTP")
    void registerUser_exitoso_retornaTrue() {
        boolean resultado = userService.registerUser(user);
        assertTrue(resultado);
    }

    @Test
    @DisplayName("registerUser con Observer: añade el observer y registra")
    void registerUser_conObserver_exitoso() {
        boolean resultado = userService.registerUser(user, testObserver);

        assertTrue(resultado);
        assertEquals(1, notificaciones.size());
        assertTrue(notificaciones.get(0).contains("Usuario registrado"));
    }

    @Test
    @DisplayName("registerUser: retorna false si el cliente lanza excepción")
    void registerUser_fallido_retornaFalse() {
        simularExcepcion = true;
        userService.attach(testObserver);

        boolean resultado = userService.registerUser(user);

        assertFalse(resultado);
        assertEquals(1, notificaciones.size());
        assertTrue(notificaciones.get(0).contains("Registro fallido"));
    }

    @Test
    @DisplayName("authenticate: retorna usuario correctamente formado si el login es exitoso")
    void authenticate_exitoso_retornaUsuario() {
        userService.attach(testObserver);

        User logueado = userService.authenticate("testuser", "password123");

        assertNotNull(logueado);
        assertEquals("testuser", logueado.getUsername());
        assertEquals(UserRole.PACIENTE, logueado.getRole());
        assertEquals(UserStatus.ACTIVE, logueado.getStatus());

        assertEquals(1, notificaciones.size());
        assertTrue(notificaciones.get(0).contains("Login exitoso"));
    }

    @Test
    @DisplayName("authenticate: retorna nulo si el cliente devuelve nulo")
    void authenticate_nulo_retornaNull() {
        simularExitoLogin = false;
        userService.attach(testObserver);

        User logueado = userService.authenticate("testuser", "wrongpassword");

        assertNull(logueado);
        assertEquals(1, notificaciones.size());
        assertTrue(notificaciones.get(0).contains("Login fallido"));
    }

    @Test
    @DisplayName("authenticate: retorna nulo y notifica si el cliente lanza excepción (ej. HTTP 401)")
    void authenticate_excepcion_retornaNull() {
        simularExcepcion = true;
        userService.attach(testObserver);

        User logueado = userService.authenticate("testuser", "wrongpassword");

        assertNull(logueado);
        assertEquals(1, notificaciones.size());
        assertTrue(notificaciones.get(0).contains("Login fallido"));
    }
}
