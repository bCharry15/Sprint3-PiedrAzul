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

        if (paciente.getGenero() != null) {
            request.setGenero(paciente.getGenero().name());
        } else {
            request.setGenero(Genero.OTRO.name());
        }

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

    @Override
    public Cita cambiarEstadoCita(Long citaId, String estado, String observacion) {
        CitaResponse response = agendaServiceClient.cambiarEstadoCita(citaId, estado, observacion);

        Medico medico = new Medico();

        if (response != null && response.getMedicoId() != null) {
            medico.setId(response.getMedicoId());
        }

        return convertirACita(response, null, medico);
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

            if (response.getPaciente() != null && !response.getPaciente().trim().isEmpty()) {
                pacienteConvertido.setNombres(response.getPaciente());
                pacienteConvertido.setApellidos("");
            } else {
                pacienteConvertido.setNombres("");
                pacienteConvertido.setApellidos("");
            }

            if (response.getDocumento() != null) {
                pacienteConvertido.setNumeroDocumento(response.getDocumento());
            } else {
                pacienteConvertido.setNumeroDocumento("");
            }
        }

        if (medico == null) {
            medico = new Medico();

            if (response.getMedicoId() != null) {
                medico.setId(response.getMedicoId());
            }
        }

        cita.setPaciente(pacienteConvertido);
        cita.setMedico(medico);
        cita.setFecha(response.getFecha());
        cita.setHora(response.getHora());
        cita.setObservacion(response.getObservacion());
        cita.setEstado(convertirEstadoSeguro(response.getEstado()));

        return cita;
    }

    private EstadoCita convertirEstadoSeguro(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return EstadoCita.PROGRAMADA;
        }

        try {
            return EstadoCita.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            System.err.println("Estado de cita no reconocido desde backend: " + estado);
            return EstadoCita.PROGRAMADA;
        }
    }
}