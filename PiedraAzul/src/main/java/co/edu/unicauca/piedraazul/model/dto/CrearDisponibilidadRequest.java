package co.edu.unicauca.piedraazul.model.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class CrearDisponibilidadRequest {

    private Long medicoId;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer intervaloMinutos;
    private Integer ventanaSemanas;

    public CrearDisponibilidadRequest() {
    }

    public CrearDisponibilidadRequest(Long medicoId,
                                      DayOfWeek diaSemana,
                                      LocalTime horaInicio,
                                      LocalTime horaFin,
                                      Integer intervaloMinutos,
                                      Integer ventanaSemanas) {
        this.medicoId = medicoId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.intervaloMinutos = intervaloMinutos;
        this.ventanaSemanas = ventanaSemanas;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public void setMedicoId(Long medicoId) {
        this.medicoId = medicoId;
    }

    public DayOfWeek getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(DayOfWeek diaSemana) {
        this.diaSemana = diaSemana;
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

    public Integer getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setIntervaloMinutos(Integer intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }

    public Integer getVentanaSemanas() {
        return ventanaSemanas;
    }

    public void setVentanaSemanas(Integer ventanaSemanas) {
        this.ventanaSemanas = ventanaSemanas;
    }
}