package co.edu.unicauca.piedraazul.agenda.pattern.builder;

import co.edu.unicauca.piedraazul.agenda.model.dto.CitaResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias - CitaResponseBuilder (patrón Builder)")
class CitaResponseBuilderTest {

    @Test
    @DisplayName("build() genera un CitaResponse con todos los campos asignados")
    void builder_todosLosCampos_generaRespuestaCompleta() {
        LocalDate fecha = LocalDate.of(2026, 8, 10);
        LocalTime hora = LocalTime.of(11, 30);

        CitaResponse response = CitaResponseBuilder.builder()
                .id(1L)
                .pacienteId(10L)
                .paciente("Ana Pérez")
                .medicoId(5L)
                .medico("Dr. Herrera")
                .fecha(fecha)
                .hora(hora)
                .estado("PROGRAMADA")
                .observacion("Revisión mensual")
                .build();

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getPacienteId());
        assertEquals("Ana Pérez", response.getPaciente());
        assertEquals(5L, response.getMedicoId());
        assertEquals("Dr. Herrera", response.getMedico());
        assertEquals(fecha, response.getFecha());
        assertEquals(hora, response.getHora());
        assertEquals("PROGRAMADA", response.getEstado());
        assertEquals("Revisión mensual", response.getObservacion());
    }

    @Test
    @DisplayName("build() permite observacion nula")
    void builder_observacionNula_construyeCorrectamente() {
        CitaResponse response = CitaResponseBuilder.builder()
                .id(2L)
                .pacienteId(20L)
                .paciente("Luis Mora")
                .medicoId(3L)
                .medico("Dra. Vargas")
                .fecha(LocalDate.now())
                .hora(LocalTime.of(9, 0))
                .estado("PROGRAMADA")
                .observacion(null)
                .build();

        assertNull(response.getObservacion());
    }

    @Test
    @DisplayName("build() con estado COMPLETADA se almacena correctamente")
    void builder_estadoCompletada_almacenaCorrectamente() {
        CitaResponse response = CitaResponseBuilder.builder()
                .id(3L)
                .estado("COMPLETADA")
                .build();

        assertEquals("COMPLETADA", response.getEstado());
    }

    @Test
    @DisplayName("build() con estado CANCELADA se almacena correctamente")
    void builder_estadoCancelada_almacenaCorrectamente() {
        CitaResponse response = CitaResponseBuilder.builder()
                .id(4L)
                .estado("CANCELADA")
                .build();

        assertEquals("CANCELADA", response.getEstado());
    }

    @Test
    @DisplayName("Cada llamada a builder() genera una instancia independiente")
    void builder_generaInstanciasIndependientes() {
        CitaResponse r1 = CitaResponseBuilder.builder().id(1L).estado("PROGRAMADA").build();
        CitaResponse r2 = CitaResponseBuilder.builder().id(2L).estado("COMPLETADA").build();

        assertNotEquals(r1.getId(), r2.getId());
        assertNotEquals(r1.getEstado(), r2.getEstado());
    }

    @Test
    @DisplayName("build() permite encadenamiento fluente completo (fluent API)")
    void builder_encadenamientoFluente_funciona() {
        assertDoesNotThrow(() -> CitaResponseBuilder.builder()
                .id(10L)
                .pacienteId(1L)
                .paciente("Nombre")
                .medicoId(2L)
                .medico("Médico")
                .fecha(LocalDate.now())
                .hora(LocalTime.NOON)
                .estado("PROGRAMADA")
                .observacion("obs")
                .build()
        );
    }
}
