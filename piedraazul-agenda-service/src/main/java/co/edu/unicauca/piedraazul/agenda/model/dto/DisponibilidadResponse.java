package co.edu.unicauca.piedraazul.agenda.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class DisponibilidadResponse {

    private Long medicoId;
    private String medico;
    private LocalDate fecha;
    private Integer intervaloMinutos;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private List<LocalTime> franjasDisponibles;

    public DisponibilidadResponse() {
    }

    public DisponibilidadResponse(Long medicoId, String medico, LocalDate fecha,
                                  Integer intervaloMinutos, LocalTime horaInicio,
                                  LocalTime horaFin, List<LocalTime> franjasDisponibles) {
        this.medicoId = medicoId;
        this.medico = medico;
        this.fecha = fecha;
        this.intervaloMinutos = intervaloMinutos;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.franjasDisponibles = franjasDisponibles;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public void setMedicoId(Long medicoId) {
        this.medicoId = medicoId;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setIntervaloMinutos(Integer intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public List<LocalTime> getFranjasDisponibles() {
        return franjasDisponibles;
    }

    public void setFranjasDisponibles(List<LocalTime> franjasDisponibles) {
        this.franjasDisponibles = franjasDisponibles;
    }
}

