package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.ConfigurarDisponibilidadUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.ConfigurarDisponibilidadPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.GestionarMedicosPort;
import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.dto.ConfiguracionDisponibilidadRequest;

@Service
public class ConfigurarDisponibilidadService implements ConfigurarDisponibilidadUseCase {

    private final GestionarMedicosPort gestionarMedicosPort;
    private final ConfigurarDisponibilidadPort configurarDisponibilidadPort;

    public ConfigurarDisponibilidadService(GestionarMedicosPort gestionarMedicosPort,
                                            ConfigurarDisponibilidadPort configurarDisponibilidadPort) {
        this.gestionarMedicosPort = gestionarMedicosPort;
        this.configurarDisponibilidadPort = configurarDisponibilidadPort;
    }

    @Override
    public DisponibilidadMedico configurar(ConfiguracionDisponibilidadRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud no puede estar vacía.");
        }

        Medico medico = gestionarMedicosPort.buscarPorId(request.getMedicoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista con id: " + request.getMedicoId()
                ));

        DisponibilidadMedico disponibilidad = new DisponibilidadMedico();
        disponibilidad.setMedico(medico);
        disponibilidad.setDiaSemana(request.getDiaSemana());
        disponibilidad.setHoraInicio(request.getHoraInicio());
        disponibilidad.setHoraFin(request.getHoraFin());
        disponibilidad.setIntervaloMinutos(request.getIntervaloMinutos());
        disponibilidad.setVentanaSemanas(request.getVentanaSemanas());
        disponibilidad.setActivo(true);

        return configurarDisponibilidadPort.guardar(disponibilidad);
    }

    @Override
    public List<DisponibilidadMedico> listarPorMedico(Long medicoId) {
        Medico medico = gestionarMedicosPort.buscarPorId(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista con id: " + medicoId
                ));

        return configurarDisponibilidadPort.buscarPorMedicoActivo(medico);
    }
}