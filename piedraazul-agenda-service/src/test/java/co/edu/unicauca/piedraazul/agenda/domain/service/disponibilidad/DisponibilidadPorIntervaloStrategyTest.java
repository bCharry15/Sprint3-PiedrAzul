package co.edu.unicauca.piedraazul.agenda.domain.service.disponibilidad;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DisponibilidadPorIntervaloStrategyTest {

    private DisponibilidadPorIntervaloStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DisponibilidadPorIntervaloStrategy();
    }

    @Test
    void debeCalcularFranjasDisponiblesPorIntervalo() {
        List<LocalTime> franjas = strategy.calcularFranjasDisponibles(
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                15,
                Set.of()
        );

        assertNotNull(franjas);
        assertFalse(franjas.isEmpty());
    }
}