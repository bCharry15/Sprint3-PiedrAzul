package co.edu.unicauca.piedraazul.model.dto;

public class CitaTablaModel {

    private final Long id;
    private final String paciente;
    private final String documento;
    private final String medico;
    private final String fecha;
    private final String hora;
    private final String estado;

    public CitaTablaModel(Long id, String paciente, String documento, String medico, String fecha, String hora, String estado) {
        this.id = id;
        this.paciente = paciente;
        this.documento = documento;
        this.medico = medico;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
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

    public String getMedico() {
        return medico;
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
}