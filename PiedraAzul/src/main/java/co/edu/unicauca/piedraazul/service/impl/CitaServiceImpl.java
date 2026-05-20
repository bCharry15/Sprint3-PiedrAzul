package co.edu.unicauca.piedraazul.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.Cita;
import co.edu.unicauca.piedraazul.model.Medico;
import co.edu.unicauca.piedraazul.model.Paciente;
import co.edu.unicauca.piedraazul.model.dto.CitaResponse;
import co.edu.unicauca.piedraazul.model.dto.CitasPorMedicoFechaResponse;
import co.edu.unicauca.piedraazul.model.dto.CrearCitaRequest;
import co.edu.unicauca.piedraazul.model.enums.EstadoCita;
import co.edu.unicauca.piedraazul.model.enums.Genero;
import co.edu.unicauca.piedraazul.service.ICitaService;

@Service
public class CitaServiceImpl implements ICitaService {

    private final AgendaServiceClient agendaServiceClient;

    public CitaServiceImpl(AgendaServiceClient agendaServiceClient) {
        this.agendaServiceClient = agendaServiceClient;
    }

    @Override
    public Cita crearCita(Paciente paciente, Medico medico, LocalDate fecha,
                          LocalTime hora, String observacion) {

        CrearCitaRequest request = new CrearCitaRequest();
        request.setNumeroDocumento(paciente.getNumeroDocumento());
        request.setTipoDocumento(paciente.getTipoDocumento());
        request.setNombres(paciente.getNombres());
        request.setApellidos(paciente.getApellidos());
        request.setCelular(paciente.getCelular());
        request.setGenero(paciente.getGenero() != null ? paciente.getGenero().name() : Genero.OTRO.name());
        request.setFechaNacimiento(paciente.getFechaNacimiento());
        request.setCorreo(paciente.getCorreo());

        request.setMedicoId(medico.getId());
        request.setFecha(fecha);
        request.setHora(hora);
        request.setObservacion(observacion);

        CitaResponse response = agendaServiceClient.crearCita(request);

        return convertirACita(response, paciente, medico);
    }

    @Override
    public List<Cita> buscarPorMedicoYFecha(Medico medico, LocalDate fecha) {
        CitasPorMedicoFechaResponse response =
                agendaServiceClient.listarCitasPorMedicoYFecha(medico.getId(), fecha);

        if (response == null || response.getCitas() == null) {
            return List.of();
        }

        return response.getCitas()
                .stream()
                .map(citaResponse -> convertirACita(citaResponse, null, medico))
                .toList();
    }

    @Override
    public long contarPorMedicoYFecha(Medico medico, LocalDate fecha) {
        CitasPorMedicoFechaResponse response =
                agendaServiceClient.listarCitasPorMedicoYFecha(medico.getId(), fecha);

        if (response == null) {
            return 0;
        }

        return response.getCantidad();
    }

    private Cita convertirACita(CitaResponse response, Paciente paciente, Medico medico) {
        if (response == null) {
            return null;
        }

        Cita cita = new Cita();
        cita.setId(response.getId());

        Paciente pacienteConvertido = paciente;

        if (pacienteConvertido == null) {
            pacienteConvertido = new Paciente();

            if (response.getPacienteId() != null) {
                pacienteConvertido.setId(response.getPacienteId());
            }

            if (response.getPaciente() != null) {
                pacienteConvertido.setNombres(response.getPaciente());
                pacienteConvertido.setApellidos("");
            } else {
                pacienteConvertido.setNombres("");
                pacienteConvertido.setApellidos("");
            }

            pacienteConvertido.setNumeroDocumento("");
        }

        cita.setPaciente(pacienteConvertido);
        cita.setMedico(medico);
        cita.setFecha(response.getFecha());
        cita.setHora(response.getHora());
        cita.setObservacion(response.getObservacion());

        if (response.getEstado() != null) {
            cita.setEstado(EstadoCita.valueOf(response.getEstado()));
        } else {
            cita.setEstado(EstadoCita.PROGRAMADA);
        }

        return cita;
    }
}