package co.edu.unicauca.piedraazul.agenda.service.impl;

import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import co.edu.unicauca.piedraazul.agenda.model.enums.Genero;
import co.edu.unicauca.piedraazul.agenda.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - PacienteServiceImpl")
class PacienteServiceImplTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteServiceImpl pacienteService;

    private Paciente pacienteExistente;

    @BeforeEach
    void setUp() {
        pacienteExistente = new Paciente();
        pacienteExistente.setNumeroDocumento("555666777");
        pacienteExistente.setTipoDocumento("CC");
        pacienteExistente.setNombres("Pedro");
        pacienteExistente.setApellidos("Ramírez");
        pacienteExistente.setCelular("3109876543");
        pacienteExistente.setGenero(Genero.HOMBRE);
        pacienteExistente.setFechaNacimiento(LocalDate.of(1985, 7, 10));
        pacienteExistente.setCorreo("pedro.ramirez@correo.com");
    }

    // ─── buscarPorNumeroDocumento ──────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorNumeroDocumento: retorna el paciente cuando existe")
    void buscarPorDoc_retornaPacienteExistente() {
        when(pacienteRepository.findByNumeroDocumento("555666777"))
                .thenReturn(Optional.of(pacienteExistente));

        Paciente resultado = pacienteService.buscarPorNumeroDocumento("555666777");

        assertNotNull(resultado);
        assertEquals("Pedro", resultado.getNombres());
    }

    @Test
    @DisplayName("buscarPorNumeroDocumento: retorna null si el paciente no existe")
    void buscarPorDoc_retornaNullSiNoExiste() {
        when(pacienteRepository.findByNumeroDocumento("000000000"))
                .thenReturn(Optional.empty());

        Paciente resultado = pacienteService.buscarPorNumeroDocumento("000000000");

        assertNull(resultado);
    }

    @Test
    @DisplayName("buscarPorNumeroDocumento: retorna null si el documento es nulo")
    void buscarPorDoc_retornaNullSiDocumentoEsNulo() {
        Paciente resultado = pacienteService.buscarPorNumeroDocumento(null);

        assertNull(resultado);
        verify(pacienteRepository, never()).findByNumeroDocumento(any());
    }

    @Test
    @DisplayName("buscarPorNumeroDocumento: retorna null si el documento está vacío")
    void buscarPorDoc_retornaNullSiDocumentoEsVacio() {
        Paciente resultado = pacienteService.buscarPorNumeroDocumento("   ");

        assertNull(resultado);
        verify(pacienteRepository, never()).findByNumeroDocumento(any());
    }

    @Test
    @DisplayName("buscarPorNumeroDocumento: recorta espacios antes de buscar")
    void buscarPorDoc_recortaEspacios() {
        when(pacienteRepository.findByNumeroDocumento("555666777"))
                .thenReturn(Optional.of(pacienteExistente));

        Paciente resultado = pacienteService.buscarPorNumeroDocumento("  555666777  ");

        assertNotNull(resultado);
        verify(pacienteRepository).findByNumeroDocumento("555666777");
    }

    // ─── obtenerOCrearPaciente ─────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerOCrearPaciente: retorna paciente existente sin crear uno nuevo")
    void obtenerOCrear_pacienteExistente_noCreaUnoNuevo() {
        when(pacienteRepository.findByNumeroDocumento("555666777"))
                .thenReturn(Optional.of(pacienteExistente));

        Paciente resultado = pacienteService.obtenerOCrearPaciente(
                "555666777", "CC", "Pedro", "Ramírez",
                "3109876543", Genero.HOMBRE, LocalDate.of(1985, 7, 10),
                "pedro.ramirez@correo.com"
        );

        assertEquals(pacienteExistente, resultado);
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("obtenerOCrearPaciente: crea un nuevo paciente si no existe")
    void obtenerOCrear_pacienteNuevo_loCreaCorrecto() {
        when(pacienteRepository.findByNumeroDocumento("999888777"))
                .thenReturn(Optional.empty());
        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Paciente resultado = pacienteService.obtenerOCrearPaciente(
                "999888777", "CC", "Laura", "Mejía",
                "3207654321", Genero.MUJER, LocalDate.of(1995, 11, 5),
                "laura.mejia@correo.com"
        );

        assertNotNull(resultado);
        assertEquals("Laura", resultado.getNombres());
        assertEquals("Mejía", resultado.getApellidos());
        assertEquals(Genero.MUJER, resultado.getGenero());
        verify(pacienteRepository, times(1)).save(any(Paciente.class));
    }

    @Test
    @DisplayName("obtenerOCrearPaciente: el nuevo paciente tiene todos sus campos asignados")
    void obtenerOCrear_nuevoPacienteTieneTodosLosCampos() {
        when(pacienteRepository.findByNumeroDocumento("112233445"))
                .thenReturn(Optional.empty());
        when(pacienteRepository.save(any(Paciente.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LocalDate fechaNac = LocalDate.of(2000, 1, 15);

        Paciente resultado = pacienteService.obtenerOCrearPaciente(
                "112233445", "TI", "Sofía", "Vargas",
                "3151112233", Genero.MUJER, fechaNac, "sofia@correo.com"
        );

        assertEquals("112233445", resultado.getNumeroDocumento());
        assertEquals("TI", resultado.getTipoDocumento());
        assertEquals("Sofía", resultado.getNombres());
        assertEquals("Vargas", resultado.getApellidos());
        assertEquals("3151112233", resultado.getCelular());
        assertEquals(Genero.MUJER, resultado.getGenero());
        assertEquals(fechaNac, resultado.getFechaNacimiento());
        assertEquals("sofia@correo.com", resultado.getCorreo());
    }
}
