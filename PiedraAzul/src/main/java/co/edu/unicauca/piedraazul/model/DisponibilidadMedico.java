package co.edu.unicauca.piedraazul.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class DisponibilidadMedico {

    private Long id;
    private Medico medico;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer intervaloMinutos;
    private Integer ventanaSemanas;
    private Boolean activo = true;

    public DisponibilidadMedico() {
    }

    public Long getId() {
        return id;
    }

    public Medico getMedico() {
        return medico;
    }

    public DayOfWeek getDiaSemana() {
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

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public void setDiaSemana(DayOfWeek diaSemana) {
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