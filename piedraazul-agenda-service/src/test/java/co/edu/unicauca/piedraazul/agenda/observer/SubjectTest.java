package co.edu.unicauca.piedraazul.agenda.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias - Subject (patrón Observer)")
class SubjectTest {

    private Subject subject;

    @BeforeEach
    void setUp() {
        subject = new Subject();
    }

    @Test
    @DisplayName("attach: agrega un observer y lo notifica correctamente")
    void attach_observerAgregado_recibeNotificacion() {
        List<String> mensajes = new ArrayList<>();
        Observer observer = mensajes::add;

        subject.attach(observer);
        subject.notifyObservers("Hola");

        assertEquals(1, mensajes.size());
        assertEquals("Hola", mensajes.get(0));
    }

    @Test
    @DisplayName("attach: no agrega el mismo observer dos veces")
    void attach_mismOobserver_noSeDuplicaEnLaLista() {
        List<String> mensajes = new ArrayList<>();
        Observer observer = mensajes::add;

        subject.attach(observer);
        subject.attach(observer); // duplicado intencional
        subject.notifyObservers("Test");

        assertEquals(1, mensajes.size()); // solo debe recibirse una vez
    }

    @Test
    @DisplayName("attach: ignora silenciosamente un observer nulo")
    void attach_observerNulo_noLanzaExcepcion() {
        assertDoesNotThrow(() -> subject.attach(null));
        assertDoesNotThrow(() -> subject.notifyObservers("Cualquier cosa"));
    }

    @Test
    @DisplayName("detach: el observer removido deja de recibir notificaciones")
    void detach_observerRemovido_noRecibeNotificaciones() {
        List<String> mensajes = new ArrayList<>();
        Observer observer = mensajes::add;

        subject.attach(observer);
        subject.detach(observer);
        subject.notifyObservers("No deberías llegar");

        assertTrue(mensajes.isEmpty());
    }

    @Test
    @DisplayName("detach: remover observer que no estaba registrado no lanza excepción")
    void detach_observerNoRegistrado_noLanzaExcepcion() {
        Observer observer = msg -> {};
        assertDoesNotThrow(() -> subject.detach(observer));
    }

    @Test
    @DisplayName("notifyObservers: múltiples observers reciben el mismo mensaje")
    void notifyObservers_multiplesObservers_todoRecibenMensaje() {
        List<String> mensajes1 = new ArrayList<>();
        List<String> mensajes2 = new ArrayList<>();
        List<String> mensajes3 = new ArrayList<>();

        subject.attach(mensajes1::add);
        subject.attach(mensajes2::add);
        subject.attach(mensajes3::add);

        subject.notifyObservers("Broadcast");

        assertEquals("Broadcast", mensajes1.get(0));
        assertEquals("Broadcast", mensajes2.get(0));
        assertEquals("Broadcast", mensajes3.get(0));
    }

    @Test
    @DisplayName("notifyObservers: sin observers registrados no lanza excepción")
    void notifyObservers_sinObservers_noLanzaExcepcion() {
        assertDoesNotThrow(() -> subject.notifyObservers("Mensaje sin destinatario"));
    }

    @Test
    @DisplayName("Combinación de attach/detach funciona en escenario real")
    void escenarioCompleto_attachDetach_funcionaCorrectamente() {
        List<String> log = new ArrayList<>();
        Observer obs1 = msg -> log.add("OBS1: " + msg);
        Observer obs2 = msg -> log.add("OBS2: " + msg);

        subject.attach(obs1);
        subject.attach(obs2);
        subject.notifyObservers("Evento A"); // ambos reciben

        subject.detach(obs1);
        subject.notifyObservers("Evento B"); // solo obs2 recibe

        assertEquals(3, log.size());
        assertTrue(log.contains("OBS1: Evento A"));
        assertTrue(log.contains("OBS2: Evento A"));
        assertTrue(log.contains("OBS2: Evento B"));
        assertFalse(log.contains("OBS1: Evento B"));
    }
}
