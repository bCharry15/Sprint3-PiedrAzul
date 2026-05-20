package co.edu.unicauca.piedraazul.model.dto;

public class CitaMedicoTablaModel {

    private final Long id;
    private final String paciente;
    private final String documento;
    private final String fecha;
    private final String hora;
    private final String estado;
    private final String observacion;

    public CitaMedicoTablaModel(
            Long id,
            String paciente,
            String documento,
            String fecha,
            String hora,
            String estado,
            String observacion
    ) {
        this.id = id;
        this.paciente = paciente;
        this.documento = documento;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.observacion = observacion;
    }

    public Long getId() {
        return id;
    }

    public String getPaciente() {
        return paciente;
    }

    public String getDocumento() {
        return documento;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public String getEstado() {
        return estado;
    }

    public String getObservacion() {
        return observacion;
    }
}