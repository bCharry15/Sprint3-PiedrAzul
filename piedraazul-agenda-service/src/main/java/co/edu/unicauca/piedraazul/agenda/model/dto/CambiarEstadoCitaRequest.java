package co.edu.unicauca.piedraazul.agenda.model.dto;

public class CambiarEstadoCitaRequest {

    private String estado;
    private String observacion;

    public CambiarEstadoCitaRequest() {
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}