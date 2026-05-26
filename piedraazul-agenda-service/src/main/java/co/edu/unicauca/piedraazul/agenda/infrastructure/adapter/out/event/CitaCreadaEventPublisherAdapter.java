package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.PublicarCitaCreadaEventPort;
import co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.event.CitaCreadaEvent;
import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.Paciente;

@Component
public class CitaCreadaEventPublisherAdapter implements PublicarCitaCreadaEventPort {

    private final ApplicationEventPublisher eventPublisher;

    public CitaCreadaEventPublisherAdapter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publicar(Cita citaCreada) {
        Paciente paciente = citaCreada.getPaciente();
        Medico medico = citaCreada.getMedico();

        eventPublisher.publishEvent(new CitaCreadaEvent(
                citaCreada.getId(),
                paciente.getId(),
                paciente.getNombreCompleto(),
                paciente.getCorreo(),
                paciente.getCelular(),
                medico.getId(),
                medico.getNombreCompleto(),
                citaCreada.getFecha(),
                citaCreada.getHora()
        ));
    }
}