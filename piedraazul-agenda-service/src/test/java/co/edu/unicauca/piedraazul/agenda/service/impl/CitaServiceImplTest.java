package co.edu.unicauca.piedraazul.agenda.service.impl;

import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;
import co.edu.unicauca.piedraazul.agenda.repository.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - CitaServiceImpl")
class CitaServiceImplTest {

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private CitaServiceImpl citaService;

    private Medico medico;
    private Paciente paciente;
    private LocalDate fecha;
    private LocalTime hora;

    @BeforeEach
    void setUp() {
        medico = new Medico();
        medico.setNombreCompleto("Dr. Pérez");
        medico.setEspecialidad("Traumatología");
        medico.setIntervaloMinutos(30);

        paciente = new Paciente();
        paciente.setNombres("Luis");
        paciente.setApellidos("Torres");
        paciente.setNumeroDocumento("111222333");

        fecha = LocalDate.of(2026, 6, 20);
        hora = LocalTime.of(10, 0);
    }

    @Test
    @DisplayName("crearCita: guarda la cita correctamente cuando no existe conflicto")
    void crearCita_sinConflicto_guardaCorrectamente() {
        when(citaRepository.existsByMedicoAndFechaAndHora(medico, fecha, hora)).thenReturn(false);

        Cita citaGuardada = new Cita();
        citaGuardada.setPaciente(paciente);
        citaGuardada.setMedico(medico);
        citaGuardada.setFecha(fecha);
        citaGuardada.setHora(hora);
        citaGuardada.setEstado(EstadoCita.PROGRAMADA);

        when(citaRepository.save(any(Cita.class))).thenReturn(citaGuardada);

        Cita resultado = citaService.crearCita(paciente, medico, fecha, hora, "Sin observaciones");

        assertNotNull(resultado);
        assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
        assertEquals(medico, resultado.getMedico());
        assertEquals(paciente, resultado.getPaciente());
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    @DisplayName("crearCita: lanza excepción si ya existe una cita para ese médico, fecha y hora")
    void crearCita_conConflicto_lanzaExcepcion() {
        when(citaRepository.existsByMedicoAndFechaAndHora(medico, fecha, hora)).thenReturn(true);

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> citaService.crearCita(paciente, medico, fecha, hora, null)
        );

        assertTrue(excepcion.getMessage().contains("Ya existe una cita"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearCita: la cita se crea con estado PROGRAMADA siempre")
    void crearCita_siempreConEstadoProgramada() {
        when(citaRepository.existsByMedicoAndFechaAndHora(medico, fecha, hora)).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita resultado = citaService.crearCita(paciente, medico, fecha, hora, "Observación");

        assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
    }

    @Test
    @DisplayName("crearCita: la observación se almacena en la cita guardada")
    void crearCita_observacionAlmacenada() {
        when(citaRepository.existsByMedicoAndFechaAndHora(medico, fecha, hora)).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita resultado = citaService.crearCita(paciente, medico, fecha, hora, "Revisión anual");

        assertEquals("Revisión anual", resultado.getObservacion());
    }

    @Test
    @DisplayName("buscarPorMedicoYFecha: retorna la lista del repositorio")
    void buscarPorMedicoYFecha_retornaListaDelRepo() {
        Cita cita1 = new Cita();
        cita1.setHora(LocalTime.of(9, 0));
        Cita cita2 = new Cita();
        cita2.setHora(LocalTime.of(9, 30));

        when(citaRepository.findByMedicoAndFechaOrderByHoraAsc(medico, fecha))
                .thenReturn(List.of(cita1, cita2));

        List<Cita> resultado = citaService.buscarPorMedicoYFecha(medico, fecha);

        assertEquals(2, resultado.size());
        verify(citaRepository, times(1)).findByMedicoAndFechaOrderByHoraAsc(medico, fecha);
    }

    @Test
    @DisplayName("buscarPorMedicoYFecha: retorna lista vacía si no hay citas")
    void buscarPorMedicoYFecha_retornaVaciaSiNoCitas() {
        when(citaRepository.findByMedicoAndFechaOrderByHoraAsc(medico, fecha))
                .thenReturn(List.of());

        List<Cita> resultado = citaService.buscarPorMedicoYFecha(medico, fecha);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("contarPorMedicoYFecha: retorna la cantidad de citas correctamente")
    void contarPorMedicoYFecha_retornaCantidadCorrecta() {
        Cita c1 = new Cita();
        Cita c2 = new Cita();
        Cita c3 = new Cita();

        when(citaRepository.findByMedicoAndFechaOrderByHoraAsc(medico, fecha))
                .thenReturn(List.of(c1, c2, c3));

        long cantidad = citaService.contarPorMedicoYFecha(medico, fecha);

        assertEquals(3, cantidad);
    }

    @Test
    @DisplayName("contarPorMedicoYFecha: retorna 0 si no hay citas en esa fecha")
    void contarPorMedicoYFecha_retornaCeroSinCitas() {
        when(citaRepository.findByMedicoAndFechaOrderByHoraAsc(medico, fecha))
                .thenReturn(List.of());

        long cantidad = citaService.contarPorMedicoYFecha(medico, fecha);

        assertEquals(0, cantidad);
    }
}
