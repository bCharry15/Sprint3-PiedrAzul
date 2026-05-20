package co.edu.unicauca.piedraazul.agenda.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.client.NotificationServiceClient;

@Component
public class CitaCreadaEventListener {

    private final NotificationServiceClient notificationServiceClient;

    public CitaCreadaEventListener(NotificationServiceClient notificationServiceClient) {
        this.notificationServiceClient = notificationServiceClient;
    }

    @EventListener
    public void manejarCitaCreada(CitaCreadaEvent event) {
        System.out.println("AGENDA-SERVICE -> EVENTO CITA_CREADA");
        System.out.println("Cita ID: " + event.getCitaId());
        System.out.println("Paciente: " + event.getPaciente());
        System.out.println("Médico/Terapista: " + event.getMedico());
        System.out.println("Fecha: " + event.getFecha());
        System.out.println("Hora: " + event.getHora());
        System.out.println("Correo paciente: " + event.getCorreoPaciente());
        System.out.println("Celular paciente: " + event.getCelularPaciente());

        notificationServiceClient.notificarCitaCreada(event);
    }
}