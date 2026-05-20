package co.edu.unicauca.piedraazul.agenda.model;

import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias - Modelo Cita")
class CitaTest {

    private Cita cita;
    private Paciente paciente;
    private Medico medico;

    @BeforeEach
    void setUp() {
        paciente = new Paciente();
        paciente.setNombres("Carlos");
        paciente.setApellidos("Gómez");
        paciente.setNumeroDocumento("123456789");

        medico = new Medico();
        medico.setNombreCompleto("Dr. López");
        medico.setEspecialidad("Cardiología");
        medico.setIntervaloMinutos(30);

        cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFecha(LocalDate.of(2026, 6, 15));
        cita.setHora(LocalTime.of(9, 0));
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setObservacion("Consulta de control");
    }

    @Test
    @DisplayName("Una cita nueva tiene estado PROGRAMADA por defecto al asignarlo")
    void citaTieneEstadoProgramada() {
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
    }

    @Test
    @DisplayName("Se puede cambiar el estado de la cita a COMPLETADA")
    void cambiarEstadoACompletada() {
        cita.setEstado(EstadoCita.COMPLETADA);
        assertEquals(EstadoCita.COMPLETADA, cita.getEstado());
    }

    @Test
    @DisplayName("Se puede cambiar el estado de la cita a CANCELADA")
    void cambiarEstadoACancelada() {
        cita.setEstado(EstadoCita.CANCELADA);
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
    }

    @Test
    @DisplayName("La cita almacena correctamente al paciente asignado")
    void citaGuardaPacienteCorrectamente() {
        assertEquals(paciente, cita.getPaciente());
        assertEquals("Carlos", cita.getPaciente().getNombres());
    }

    @Test
    @DisplayName("La cita almacena correctamente al médico asignado")
    void citaGuardaMedicoCorrectamente() {
        assertEquals(medico, cita.getMedico());
        assertEquals("Dr. López", cita.getMedico().getNombreCompleto());
    }

    @Test
    @DisplayName("La cita almacena correctamente la fecha y la hora")
    void citaGuardaFechaYHoraCorrectamente() {
        assertEquals(LocalDate.of(2026, 6, 15), cita.getFecha());
        assertEquals(LocalTime.of(9, 0), cita.getHora());
    }

    @Test
    @DisplayName("La cita almacena correctamente la observación")
    void citaGuardaObservacionCorrectamente() {
        assertEquals("Consulta de control", cita.getObservacion());
    }

    @Test
    @DisplayName("La observación puede ser nula")
    void observacionPuedeSerNula() {
        cita.setObservacion(null);
        assertNull(cita.getObservacion());
    }

    @Test
    @DisplayName("La cita puede actualizar su fecha")
    void actualizarFecha() {
        LocalDate nuevaFecha = LocalDate.of(2026, 7, 20);
        cita.setFecha(nuevaFecha);
        assertEquals(nuevaFecha, cita.getFecha());
    }

    @Test
    @DisplayName("La cita puede actualizar su hora")
    void actualizarHora() {
        LocalTime nuevaHora = LocalTime.of(14, 30);
        cita.setHora(nuevaHora);
        assertEquals(nuevaHora, cita.getHora());
    }
}
