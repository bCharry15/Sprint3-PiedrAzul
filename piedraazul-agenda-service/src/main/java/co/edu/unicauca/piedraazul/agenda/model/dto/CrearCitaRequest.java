package co.edu.unicauca.piedraazul.agenda.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CrearCitaRequest {

    private String numeroDocumento;
    private String tipoDocumento;
    private String nombres;
    private String apellidos;
    private String celular;
    private String genero;
    private LocalDate fechaNacimiento;
    private String correo;

    private Long medicoId;
    private LocalDate fecha;
    private LocalTime hora;
    private String observacion;

    public CrearCitaRequest() {
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getCelular() {
        return celular;
    }

    public String getGenero() {
        return genero;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getCorreo() {
        return correo;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setMedicoId(Long medicoId) {
        this.medicoId = medicoId;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}

