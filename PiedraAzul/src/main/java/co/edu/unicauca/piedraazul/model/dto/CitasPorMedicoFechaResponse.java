package co.edu.unicauca.piedraazul.model.dto;

import java.time.LocalDate;
import java.util.List;

public class CitasPorMedicoFechaResponse {

    private Long medicoId;
    private String medico;
    private LocalDate fecha;
    private long cantidad;
    private List<CitaResponse> citas;

    public CitasPorMedicoFechaResponse() {
    }

    public CitasPorMedicoFechaResponse(Long medicoId, String medico, LocalDate fecha,
                                       long cantidad, List<CitaResponse> citas) {
        this.medicoId = medicoId;
        this.medico = medico;
        this.fecha = fecha;
        this.cantidad = cantidad;
        this.citas = citas;
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

    public long getCantidad() {
        return cantidad;
    }

    public List<CitaResponse> getCitas() {
        return citas;
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

    public void setCantidad(long cantidad) {
        this.cantidad = cantidad;
    }

    public void setCitas(List<CitaResponse> citas) {
        this.citas = citas;
    }
}