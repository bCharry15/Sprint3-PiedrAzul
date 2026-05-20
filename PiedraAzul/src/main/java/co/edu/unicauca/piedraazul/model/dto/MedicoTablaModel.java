package co.edu.unicauca.piedraazul.model.dto;

public class MedicoTablaModel {

    private final Long id;
    private final String nombreCompleto;
    private final String especialidad;
    private final Integer intervaloMinutos;
    private final String username;

    public MedicoTablaModel(Long id, String nombreCompleto, String especialidad, Integer intervaloMinutos, String username) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.intervaloMinutos = intervaloMinutos;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public Integer getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public String getUsername() {
        return username;
    }
}