package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.notification;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.NotificarCitaCreadaPort;

@Component
public class NotificationRestAdapter implements NotificarCitaCreadaPort {

    @Override
    public void notificarCitaCreada(
            Long citaId,
            String nombrePaciente,
            String correoPaciente,
            String celularPaciente,
            String nombreMedico,
            LocalDate fecha,
            LocalTime hora
    ) {
        // Pendiente de conectar con el cliente HTTP actual de notification-service.
        System.out.println("HEXAGONAL OUT PORT -> Notificación de cita creada: " + citaId);
    }
}