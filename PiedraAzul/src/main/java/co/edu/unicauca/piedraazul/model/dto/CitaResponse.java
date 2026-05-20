package co.edu.unicauca.piedraazul.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaResponse {

    private Long id;
    private Long pacienteId;
    private String paciente;
    private Long medicoId;
    private String medico;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;
    private String observacion;

    public CitaResponse() {
    }

    public CitaResponse(Long id, Long pacienteId, String paciente, Long medicoId, String medico,
                        LocalDate fecha, LocalTime hora, String estado, String observacion) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.paciente = paciente;
        this.medicoId = medicoId;
        this.medico = medico;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.observacion = observacion;
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public String getPaciente() {
        return paciente;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public String getMedico() {
        return medico;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getEstado() {
        return estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public void setMedicoId(Long medicoId) {
        this.medicoId = medicoId;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}