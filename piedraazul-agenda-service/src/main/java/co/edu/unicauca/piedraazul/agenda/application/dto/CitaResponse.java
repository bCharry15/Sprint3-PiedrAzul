package co.edu.unicauca.piedraazul.agenda.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaResponse {

    private Long id;
    private String numeroDocumento;
    private String nombrePaciente;
    private String nombreMedico;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;

    public CitaResponse() {
    }

    public CitaResponse(
            Long id,
            String numeroDocumento,
            String nombrePaciente,
            String nombreMedico,
            LocalDate fecha,
            LocalTime hora,
            String estado
    ) {
        this.id = id;
        this.numeroDocumento = numeroDocumento;
        this.nombrePaciente = nombrePaciente;
        this.nombreMedico = nombreMedico;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public String getNombreMedico() {
        return nombreMedico;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getEstado() {
        return estado;
    }
}