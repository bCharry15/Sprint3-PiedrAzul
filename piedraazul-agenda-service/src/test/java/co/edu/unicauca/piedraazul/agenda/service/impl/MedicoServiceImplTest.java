package co.edu.unicauca.piedraazul.agenda.service.impl;

import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.agenda.repository.MedicoRepository;
import co.edu.unicauca.piedraazul.agenda.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - MedicoServiceImpl")
class MedicoServiceImplTest {

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private UserRepository userRepository;

    // BCryptPasswordEncoder se instancia directamente (no necesita ser mock)
    // Solo los repositorios requieren mock para aislar las pruebas de la BD
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private MedicoServiceImpl medicoService;

    private Medico medicoExistente;

    @BeforeEach
    void setUp() {
        medicoService = new MedicoServiceImpl(medicoRepository, userRepository, passwordEncoder);

        medicoExistente = new Medico();
        medicoExistente.setNombreCompleto("Dr. Castro");
        medicoExistente.setEspecialidad("Pediatría");
        medicoExistente.setIntervaloMinutos(20);
    }

    @Test
    @DisplayName("listarTodos: retorna todos los médicos del repositorio")
    void listarTodos_retornaTodosLosMedicos() {
        Medico m2 = new Medico();
        m2.setNombreCompleto("Dra. Gómez");
        when(medicoRepository.findAll()).thenReturn(List.of(medicoExistente, m2));

        List<Medico> resultado = medicoService.listarTodos();

        assertEquals(2, resultado.size());
        verify(medicoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("listarTodos: retorna lista vacía si no hay médicos")
    void listarTodos_retornaVaciaSiNoHayMedicos() {
        when(medicoRepository.findAll()).thenReturn(List.of());

        List<Medico> resultado = medicoService.listarTodos();

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("buscarPorId: retorna el médico cuando existe")
    void buscarPorId_retornaMedicoExistente() {
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medicoExistente));

        Optional<Medico> resultado = medicoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Dr. Castro", resultado.get().getNombreCompleto());
    }

    @Test
    @DisplayName("buscarPorId: retorna Optional vacío si no existe")
    void buscarPorId_retornaVacioSiNoExiste() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Medico> resultado = medicoService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("buscarPorUsernameUsuario: retorna el médico asociado al username")
    void buscarPorUsernameUsuario_retornaMedico() {
        when(medicoRepository.findByUserUsername("dr.castro")).thenReturn(Optional.of(medicoExistente));

        Optional<Medico> resultado = medicoService.buscarPorUsernameUsuario("dr.castro");

        assertTrue(resultado.isPresent());
        assertEquals("Dr. Castro", resultado.get().getNombreCompleto());
    }

    @Test
    @DisplayName("registrarMedico: registra correctamente cuando el username no existe")
    void registrarMedico_usernameNuevo_registraCorrectamente() {
        when(userRepository.findByUsername("nuevo.medico")).thenReturn(Optional.empty());

        User userGuardado = new User();
        userGuardado.setUsername("nuevo.medico");
        userGuardado.setRole(UserRole.MEDICO);
        userGuardado.setStatus(UserStatus.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(userGuardado);

        Medico medicoGuardado = new Medico();
        medicoGuardado.setNombreCompleto("Dr. Nuevo");
        medicoGuardado.setEspecialidad("Ortopedia");
        medicoGuardado.setIntervaloMinutos(30);
        medicoGuardado.setUser(userGuardado);
        when(medicoRepository.save(any(Medico.class))).thenReturn(medicoGuardado);

        Medico resultado = medicoService.registrarMedico(
                "Dr. Nuevo", "Ortopedia", 30, "nuevo.medico", "password123"
        );

        assertNotNull(resultado);
        assertEquals("Dr. Nuevo", resultado.getNombreCompleto());
        assertEquals(UserRole.MEDICO, resultado.getUser().getRole());
        verify(userRepository, times(1)).save(any(User.class));
        verify(medicoRepository, times(1)).save(any(Medico.class));
    }

    @Test
    @DisplayName("registrarMedico: lanza excepción si el username ya existe")
    void registrarMedico_usernameExistente_lanzaExcepcion() {
        User userExistente = new User();
        userExistente.setUsername("medico.existente");
        when(userRepository.findByUsername("medico.existente")).thenReturn(Optional.of(userExistente));

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> medicoService.registrarMedico(
                        "Dr. Repetido", "Cardiología", 30, "medico.existente", "clave"
                )
        );

        assertTrue(excepcion.getMessage().contains("ya existe"));
        verify(medicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarMedico: la contraseña del usuario queda encriptada (no en texto plano)")
    void registrarMedico_passwordQuedaEncriptada() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(medicoRepository.save(any(Medico.class))).thenAnswer(inv -> inv.getArgument(0));

        Medico resultado = medicoService.registrarMedico(
                "Dra. Validación", "Medicina General", 15, "dra.val", "clave123"
        );

        // La contraseña almacenada no debe ser el texto plano
        assertNotNull(resultado.getUser());
        assertNotEquals("clave123", resultado.getUser().getPassword());
        // Debe ser un hash BCrypt válido (comienza con $2a$ o $2b$)
        assertTrue(resultado.getUser().getPassword().startsWith("$2"));
    }

    @Test
    @DisplayName("registrarMedico: el usuario creado tiene rol MEDICO y estado ACTIVE")
    void registrarMedico_usuarioTieneRolMedicoYEstadoActivo() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(medicoRepository.save(any(Medico.class))).thenAnswer(inv -> inv.getArgument(0));

        Medico resultado = medicoService.registrarMedico(
                "Dra. Validación", "Medicina General", 15, "dra.val", "abc123"
        );

        assertNotNull(resultado.getUser());
        assertEquals(UserRole.MEDICO, resultado.getUser().getRole());
        assertEquals(UserStatus.ACTIVE, resultado.getUser().getStatus());
    }

    @Test
    @DisplayName("guardar: delega al repositorio y retorna el médico guardado")
    void guardar_delegaAlRepositorio() {
        when(medicoRepository.save(medicoExistente)).thenReturn(medicoExistente);

        Medico resultado = medicoService.guardar(medicoExistente);

        assertEquals(medicoExistente, resultado);
        verify(medicoRepository, times(1)).save(medicoExistente);
    }
}
