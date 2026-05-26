package co.edu.unicauca.piedraazul.pattern.builder;

import java.time.LocalDate;
import java.time.LocalTime;

import co.edu.unicauca.piedraazul.model.dto.CitaResponse;

public class CitaResponseBuilder {

    private Long id;
    private Long pacienteId;
    private String paciente;
    private String documento;
    private Long medicoId;
    private String medico;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;
    private String observacion;

    private CitaResponseBuilder() {
    }

    public static CitaResponseBuilder builder() {
        return new CitaResponseBuilder();
    }

    public CitaResponseBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public CitaResponseBuilder pacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
        return this;
    }

    public CitaResponseBuilder paciente(String paciente) {
        this.paciente = paciente;
        return this;
    }

    public CitaResponseBuilder documento(String documento) {
        this.documento = documento;
        return this;
    }

    public CitaResponseBuilder medicoId(Long medicoId) {
        this.medicoId = medicoId;
        return this;
    }

    public CitaResponseBuilder medico(String medico) {
        this.medico = medico;
        return this;
    }

    public CitaResponseBuilder fecha(LocalDate fecha) {
        this.fecha = fecha;
        return this;
    }

    public CitaResponseBuilder hora(LocalTime hora) {
        this.hora = hora;
        return this;
    }

    public CitaResponseBuilder estado(String estado) {
        this.estado = estado;
        return this;
    }

    public CitaResponseBuilder observacion(String observacion) {
        this.observacion = observacion;
        return this;
    }

    public CitaResponse build() {
        CitaResponse response = new CitaResponse();

        response.setId(id);
        response.setPacienteId(pacienteId);
        response.setPaciente(paciente);
        response.setDocumento(documento);
        response.setMedicoId(medicoId);
        response.setMedico(medico);
        response.setFecha(fecha);
        response.setHora(hora);
        response.setEstado(estado);
        response.setObservacion(observacion);

        return response;
    }
}