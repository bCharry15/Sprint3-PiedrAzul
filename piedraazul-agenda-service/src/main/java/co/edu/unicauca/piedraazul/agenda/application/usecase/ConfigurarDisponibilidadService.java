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
        validarSolicitud(request);

        Medico medico = buscarMedico(request.getMedicoId());

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
    public DisponibilidadMedico actualizar(Long disponibilidadId, ConfiguracionDisponibilidadRequest request) {
        if (disponibilidadId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id de la disponibilidad es obligatorio.");
        }

        validarSolicitud(request);

        DisponibilidadMedico disponibilidad = configurarDisponibilidadPort.buscarPorId(disponibilidadId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe una configuración de disponibilidad con id: " + disponibilidadId
                ));

        Medico medico = buscarMedico(request.getMedicoId());

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
        if (medicoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id del médico es obligatorio.");
        }

        Medico medico = buscarMedico(medicoId);

        return configurarDisponibilidadPort.buscarPorMedicoActivo(medico);
    }

    private Medico buscarMedico(Long medicoId) {
        if (medicoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id del médico es obligatorio.");
        }

        return gestionarMedicosPort.buscarPorId(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista con id: " + medicoId
                ));
    }

    private void validarSolicitud(ConfiguracionDisponibilidadRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud no puede estar vacía.");
        }

        if (request.getMedicoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id del médico es obligatorio.");
        }

        if (request.getDiaSemana() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El día de la semana es obligatorio.");
        }

        if (request.getHoraInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de inicio es obligatoria.");
        }

        if (request.getHoraFin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de fin es obligatoria.");
        }

        if (!request.getHoraInicio().isBefore(request.getHoraFin())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La hora de inicio debe ser menor que la hora de fin."
            );
        }

        if (request.getIntervaloMinutos() == null || request.getIntervaloMinutos() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El intervalo en minutos debe ser mayor que cero."
            );
        }

        if (request.getVentanaSemanas() == null || request.getVentanaSemanas() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La ventana de semanas debe ser mayor que cero."
            );
        }
    }
}