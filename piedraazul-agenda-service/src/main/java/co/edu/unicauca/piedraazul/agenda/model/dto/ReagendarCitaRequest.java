package co.edu.unicauca.piedraazul.agenda.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReagendarCitaRequest {

    private LocalDate fechaNueva;
    private LocalTime horaNueva;
    private String responsable;
    private String motivo;

    public ReagendarCitaRequest() {
    }

    public LocalDate getFechaNueva() {
        return fechaNueva;
    }

    public void setFechaNueva(LocalDate fechaNueva) {
        this.fechaNueva = fechaNueva;
    }

    public LocalTime getHoraNueva() {
        return horaNueva;
    }

    public void setHoraNueva(LocalTime horaNueva) {
        this.horaNueva = horaNueva;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}