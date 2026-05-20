package co.edu.unicauca.piedraazul.agenda.pattern.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias - DisponibilidadPorIntervaloStrategy")
class DisponibilidadPorIntervaloStrategyTest {

    private DisponibilidadPorIntervaloStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DisponibilidadPorIntervaloStrategy();
    }

    @Test
    @DisplayName("Calcula franjas disponibles correctamente sin horas ocupadas")
    void calcularFranjas_sinHorasOcupadas_retornaTodasLasFranjas() {
        LocalTime inicio = LocalTime.of(8, 0);
        LocalTime fin = LocalTime.of(10, 0);
        int intervalo = 30;

        List<LocalTime> franjas = strategy.calcularFranjasDisponibles(inicio, fin, intervalo, Set.of());

        // 8:00, 8:30, 9:00, 9:30 → 4 franjas
        assertEquals(4, franjas.size());
        assertEquals(LocalTime.of(8, 0), franjas.get(0));
        assertEquals(LocalTime.of(8, 30), franjas.get(1));
        assertEquals(LocalTime.of(9, 0), franjas.get(2));
        assertEquals(LocalTime.of(9, 30), franjas.get(3));
    }

    @Test
    @DisplayName("Excluye las horas que ya están ocupadas")
    void calcularFranjas_conHorasOcupadas_excluyeCorrectamente() {
        LocalTime inicio = LocalTime.of(8, 0);
        LocalTime fin = LocalTime.of(10, 0);
        int intervalo = 30;
        Set<LocalTime> ocupadas = Set.of(LocalTime.of(8, 30), LocalTime.of(9, 30));

        List<LocalTime> franjas = strategy.calcularFranjasDisponibles(inicio, fin, intervalo, ocupadas);

        assertEquals(2, franjas.size());
        assertEquals(LocalTime.of(8, 0), franjas.get(0));
        assertEquals(LocalTime.of(9, 0), franjas.get(1));
    }

    @Test
    @DisplayName("Retorna lista vacía si todas las horas están ocupadas")
    void calcularFranjas_todasOcupadas_retornaVacia() {
        LocalTime inicio = LocalTime.of(8, 0);
        LocalTime fin = LocalTime.of(9, 0);
        int intervalo = 30;
        Set<LocalTime> ocupadas = Set.of(
                LocalTime.of(8, 0),
                LocalTime.of(8, 30)
        );

        List<LocalTime> franjas = strategy.calcularFranjasDisponibles(inicio, fin, intervalo, ocupadas);

        assertTrue(franjas.isEmpty());
    }

    @Test
    @DisplayName("Retorna lista vacía si el inicio es igual al fin")
    void calcularFranjas_inicioIgualFin_retornaVacia() {
        LocalTime mismo = LocalTime.of(9, 0);

        List<LocalTime> franjas = strategy.calcularFranjasDisponibles(mismo, mismo, 30, Set.of());

        assertTrue(franjas.isEmpty());
    }

    @Test
    @DisplayName("Funciona correctamente con intervalo de 15 minutos")
    void calcularFranjas_intervalo15Min_retornaCantidadCorrecta() {
        LocalTime inicio = LocalTime.of(8, 0);
        LocalTime fin = LocalTime.of(9, 0);
        int intervalo = 15;

        List<LocalTime> franjas = strategy.calcularFranjasDisponibles(inicio, fin, intervalo, Set.of());

        // 8:00, 8:15, 8:30, 8:45 → 4 franjas
        assertEquals(4, franjas.size());
    }

    @Test
    @DisplayName("Funciona correctamente con intervalo de 60 minutos")
    void calcularFranjas_intervalo60Min_retornaFranjasHorarias() {
        LocalTime inicio = LocalTime.of(8, 0);
        LocalTime fin = LocalTime.of(12, 0);
        int intervalo = 60;

        List<LocalTime> franjas = strategy.calcularFranjasDisponibles(inicio, fin, intervalo, Set.of());

        // 8:00, 9:00, 10:00, 11:00 → 4 franjas
        assertEquals(4, franjas.size());
    }

    @Test
    @DisplayName("La hora del fin no se incluye en las franjas disponibles")
    void calcularFranjas_horaFinNoEsIncluida() {
        LocalTime inicio = LocalTime.of(8, 0);
        LocalTime fin = LocalTime.of(9, 0);
        int intervalo = 30;

        List<LocalTime> franjas = strategy.calcularFranjasDisponibles(inicio, fin, intervalo, Set.of());

        // 8:00, 8:30 → fin (9:00) NO se incluye
        assertEquals(2, franjas.size());
        assertFalse(franjas.contains(LocalTime.of(9, 0)));
    }

    @Test
    @DisplayName("Una sola franja disponible cuando sólo hay una")
    void calcularFranjas_unaFranjaDisponible() {
        LocalTime inicio = LocalTime.of(10, 0);
        LocalTime fin = LocalTime.of(10, 30);
        int intervalo = 30;

        List<LocalTime> franjas = strategy.calcularFranjasDisponibles(inicio, fin, intervalo, Set.of());

        assertEquals(1, franjas.size());
        assertEquals(LocalTime.of(10, 0), franjas.get(0));
    }
}
