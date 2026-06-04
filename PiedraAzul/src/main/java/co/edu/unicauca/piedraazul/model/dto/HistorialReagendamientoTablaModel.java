package co.edu.unicauca.piedraazul.model.dto;

public class HistorialReagendamientoTablaModel {

    private Long id;
    private Long citaId;
    private String fechaAnterior;
    private String horaAnterior;
    private String fechaNueva;
    private String horaNueva;
    private String responsable;
    private String motivo;
    private String fechaCambio;

    public HistorialReagendamientoTablaModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public String getFechaAnterior() {
        return fechaAnterior;
    }

    public void setFechaAnterior(String fechaAnterior) {
        this.fechaAnterior = fechaAnterior;
    }

    public String getHoraAnterior() {
        return horaAnterior;
    }

    public void setHoraAnterior(String horaAnterior) {
        this.horaAnterior = horaAnterior;
    }

    public String getFechaNueva() {
        return fechaNueva;
    }

    public void setFechaNueva(String fechaNueva) {
        this.fechaNueva = fechaNueva;
    }

    public String getHoraNueva() {
        return horaNueva;
    }

    public void setHoraNueva(String horaNueva) {
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

    public String getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(String fechaCambio) {
        this.fechaCambio = fechaCambio;
    }
}