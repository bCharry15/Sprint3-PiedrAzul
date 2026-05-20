package co.edu.unicauca.piedraazul.agenda.model.dto;

import java.time.LocalTime;

public class CrearDisponibilidadRequest {

    private Long medicoId;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer intervaloMinutos;
    private Integer ventanaSemanas;
    private Boolean activo;

    public CrearDisponibilidadRequest() {
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public Integer getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public Integer getVentanaSemanas() {
        return ventanaSemanas;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setMedicoId(Long medicoId) {
        this.medicoId = medicoId;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public void setIntervaloMinutos(Integer intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }

    public void setVentanaSemanas(Integer ventanaSemanas) {
        this.ventanaSemanas = ventanaSemanas;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

