package co.edu.unicauca.piedraazul.notification.service;

import co.edu.unicauca.piedraazul.notification.dto.CitaCreadaNotificationRequest;
import co.edu.unicauca.piedraazul.notification.model.NotificacionLog;
import co.edu.unicauca.piedraazul.notification.repository.NotificacionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - NotificationService")
class NotificationServiceTest {

    @Mock
    private NotificacionLogRepository notificacionLogRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private NotificationService notificationService;

    private CitaCreadaNotificationRequest request;

    @BeforeEach
    void setUp() {
        // Inyectar valores @Value manualmente (no hay contexto Spring en pruebas unitarias)
        ReflectionTestUtils.setField(notificationService, "mailEnabled", false);
        ReflectionTestUtils.setField(notificationService, "mailFrom", "no-reply@piedraazul.com");

        request = new CitaCreadaNotificationRequest();
        request.setCitaId(1L);
        request.setPaciente("Ana Torres");
        request.setMedico("Dr. Herrera");
        request.setFecha("2026-06-20");
        request.setHora("09:00");
        request.setCorreoPaciente("ana@correo.com");
        request.setCelularPaciente("3001112233");
    }

    // ─── procesarNotificacionCitaCreada (mailEnabled = false) ─────────────────

    @Test
    @DisplayName("Modo local: guarda el log con estado ENVIADO sin enviar correo")
    void procesarNotificacion_modoLocal_guardaLogEnviado() {
        notificationService.procesarNotificacionCitaCreada(request);

        ArgumentCaptor<NotificacionLog> captor = ArgumentCaptor.forClass(NotificacionLog.class);
        verify(notificacionLogRepository, times(1)).save(captor.capture());
        assertEquals("ENVIADO", captor.getValue().getEstado());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Modo local: el log almacena los datos del paciente y la cita")
    void procesarNotificacion_modoLocal_logConDatosCorrectos() {
        notificationService.procesarNotificacionCitaCreada(request);

        ArgumentCaptor<NotificacionLog> captor = ArgumentCaptor.forClass(NotificacionLog.class);
        verify(notificacionLogRepository).save(captor.capture());

        NotificacionLog log = captor.getValue();
        assertEquals(1L, log.getCitaId());
        assertEquals("Ana Torres", log.getPaciente());
        assertEquals("Dr. Herrera", log.getMedico());
        assertEquals("ana@correo.com", log.getCorreoPaciente());
        assertEquals("3001112233", log.getCelularPaciente());
        assertNotNull(log.getFechaEnvio());
    }

    @Test
    @DisplayName("Modo local: la fecha de envío del log es reciente (dentro de los últimos 5 segundos)")
    void procesarNotificacion_modoLocal_fechaEnvioEsReciente() {
        notificationService.procesarNotificacionCitaCreada(request);

        ArgumentCaptor<NotificacionLog> captor = ArgumentCaptor.forClass(NotificacionLog.class);
        verify(notificacionLogRepository).save(captor.capture());

        assertNotNull(captor.getValue().getFechaEnvio());
    }

    // ─── procesarNotificacionCitaCreada (mailEnabled = true) ──────────────────

    @Test
    @DisplayName("Modo correo activo: envía correo y guarda log con estado ENVIADO")
    void procesarNotificacion_correoActivo_enviaYGuardaLog() {
        ReflectionTestUtils.setField(notificationService, "mailEnabled", true);

        notificationService.procesarNotificacionCitaCreada(request);

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));

        ArgumentCaptor<NotificacionLog> captor = ArgumentCaptor.forClass(NotificacionLog.class);
        verify(notificacionLogRepository, times(1)).save(captor.capture());
        assertEquals("ENVIADO", captor.getValue().getEstado());
    }

    @Test
    @DisplayName("Modo correo activo: si el correo es nulo, guarda log con estado FALLIDO")
    void procesarNotificacion_correoNulo_guardaLogFallido() {
        ReflectionTestUtils.setField(notificationService, "mailEnabled", true);
        request.setCorreoPaciente(null);

        notificationService.procesarNotificacionCitaCreada(request);

        ArgumentCaptor<NotificacionLog> captor = ArgumentCaptor.forClass(NotificacionLog.class);
        verify(notificacionLogRepository, times(1)).save(captor.capture());
        assertEquals("FALLIDO", captor.getValue().getEstado());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Modo correo activo: si el correo está en blanco, guarda log con estado FALLIDO")
    void procesarNotificacion_correoBlanco_guardaLogFallido() {
        ReflectionTestUtils.setField(notificationService, "mailEnabled", true);
        request.setCorreoPaciente("   ");

        notificationService.procesarNotificacionCitaCreada(request);

        ArgumentCaptor<NotificacionLog> captor = ArgumentCaptor.forClass(NotificacionLog.class);
        verify(notificacionLogRepository).save(captor.capture());
        assertEquals("FALLIDO", captor.getValue().getEstado());
    }

    @Test
    @DisplayName("Modo correo activo: si JavaMailSender lanza excepción, guarda log FALLIDO")
    void procesarNotificacion_excepcionAlEnviar_guardaLogFallido() {
        ReflectionTestUtils.setField(notificationService, "mailEnabled", true);
        doThrow(new RuntimeException("Error SMTP")).when(javaMailSender).send(any(SimpleMailMessage.class));

        notificationService.procesarNotificacionCitaCreada(request);

        ArgumentCaptor<NotificacionLog> captor = ArgumentCaptor.forClass(NotificacionLog.class);
        verify(notificacionLogRepository).save(captor.capture());
        assertEquals("FALLIDO", captor.getValue().getEstado());
    }

    // ─── listarNotificaciones ─────────────────────────────────────────────────

    @Test
    @DisplayName("listarNotificaciones: delega al repositorio y retorna la lista completa")
    void listarNotificaciones_retornaLista() {
        NotificacionLog log1 = new NotificacionLog();
        log1.setEstado("ENVIADO");
        NotificacionLog log2 = new NotificacionLog();
        log2.setEstado("FALLIDO");

        when(notificacionLogRepository.findAll()).thenReturn(List.of(log1, log2));

        List<NotificacionLog> resultado = notificationService.listarNotificaciones();

        assertEquals(2, resultado.size());
        verify(notificacionLogRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("listarNotificaciones: retorna lista vacía si no hay registros")
    void listarNotificaciones_retornaVacia() {
        when(notificacionLogRepository.findAll()).thenReturn(List.of());

        List<NotificacionLog> resultado = notificationService.listarNotificaciones();

        assertTrue(resultado.isEmpty());
    }
}
