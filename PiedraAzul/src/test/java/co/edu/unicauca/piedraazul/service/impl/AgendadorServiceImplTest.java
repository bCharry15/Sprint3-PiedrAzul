package co.edu.unicauca.piedraazul.service.impl;

import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.enums.UserRole;
import co.edu.unicauca.piedraazul.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - AgendadorServiceImpl")
class AgendadorServiceImplTest {

    @Mock
    private IUserService userService;

    @InjectMocks
    private AgendadorServiceImpl agendadorService;

    @BeforeEach
    void setUp() {
        // En este caso AgendadorServiceImpl tiene una lista interna en memoria
        // así que se inicializa vacía con cada test.
    }

    @Test
    @DisplayName("registrarAgendador: registra con rol AGENDADOR y devuelve usuario")
    void registrarAgendador_exitoso_retornaUsuarioAgregadoALista() {
        when(userService.registerUser(any(User.class))).thenReturn(true);

        User agendador = agendadorService.registrarAgendador("juan.agendador", "secreto123");

        assertNotNull(agendador);
        assertEquals("juan.agendador", agendador.getUsername());
        assertEquals("secreto123", agendador.getPassword());
        assertEquals(UserRole.AGENDADOR, agendador.getRole());
        assertEquals(UserStatus.ACTIVE, agendador.getStatus());

        List<User> lista = agendadorService.listarAgendadores();
        assertEquals(1, lista.size());
        assertEquals(agendador, lista.get(0));

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    @DisplayName("registrarAgendador: lanza excepción si UserService retorna false (ej. duplicado)")
    void registrarAgendador_fallido_lanzaExcepcion() {
        when(userService.registerUser(any(User.class))).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> agendadorService.registrarAgendador("juan.agendador", "clave")
        );

        assertTrue(ex.getMessage().contains("ya existe"));
        assertTrue(agendadorService.listarAgendadores().isEmpty());
    }

    @Test
    @DisplayName("listarAgendadores: retorna la lista interna")
    void listarAgendadores_retornaListaCorrecta() {
        when(userService.registerUser(any(User.class))).thenReturn(true);

        agendadorService.registrarAgendador("agendador1", "123");
        agendadorService.registrarAgendador("agendador2", "123");

        List<User> lista = agendadorService.listarAgendadores();

        assertEquals(2, lista.size());
        assertEquals("agendador1", lista.get(0).getUsername());
        assertEquals("agendador2", lista.get(1).getUsername());
    }
}
