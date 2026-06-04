package co.edu.unicauca.piedraazul.model.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class DisponibilidadTablaModel {

    private Long id;
    private Long medicoId;
    private String medicoNombre;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer intervaloMinutos;
    private Integer ventanaSemanas;
    private Boolean activo;

    public DisponibilidadTablaModel() {
    }

    public DisponibilidadTablaModel(Long id,
                                    Long medicoId,
                                    String medicoNombre,
                                    DayOfWeek diaSemana,
                                    LocalTime horaInicio,
                                    LocalTime horaFin,
                                    Integer intervaloMinutos,
                                    Integer ventanaSemanas,
                                    Boolean activo) {
        this.id = id;
        this.medicoId = medicoId;
        this.medicoNombre = medicoNombre;
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

    public String getMedicoNombre() {
        return medicoNombre;
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

    public String getDiaSemanaTexto() {
        if (diaSemana == null) {
            return "";
        }

        return switch (diaSemana) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }

    public String getHoraInicioTexto() {
        return horaInicio == null ? "" : horaInicio.toString();
    }

    public String getHoraFinTexto() {
        return horaFin == null ? "" : horaFin.toString();
    }

    public String getActivoTexto() {
        return Boolean.TRUE.equals(activo) ? "Activo" : "Inactivo";
    }
}