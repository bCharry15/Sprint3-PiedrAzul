package co.edu.unicauca.piedraazul.agenda.model.dto;

import java.time.LocalTime;

public class DisponibilidadMedicoResponse {

    private Long id;
    private Long medicoId;
    private String medico;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer intervaloMinutos;
    private Integer ventanaSemanas;
    private Boolean activo;

    public DisponibilidadMedicoResponse() {
    }

    public DisponibilidadMedicoResponse(Long id, Long medicoId, String medico, String diaSemana,
                                        LocalTime horaInicio, LocalTime horaFin,
                                        Integer intervaloMinutos, Integer ventanaSemanas,
                                        Boolean activo) {
        this.id = id;
        this.medicoId = medicoId;
        this.medico = medico;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.intervaloMinutos = intervaloMinutos;
        this.ventanaSemanas = ventanaSemanas;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public String getMedico() {
        return medico;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setMedicoId(Long medicoId) {
        this.medicoId = medicoId;
    }

    public void setMedico(String medico) {
        this.medico = medico;
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

