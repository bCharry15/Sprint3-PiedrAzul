package co.edu.unicauca.piedraazul.model.dto;

public class MedicoResponse {

    private Long id;
    private String nombreCompleto;
    private String especialidad;
    private Integer intervaloMinutos;
    private String username;

    public MedicoResponse() {
    }

    public MedicoResponse(Long id, String nombreCompleto, String especialidad, Integer intervaloMinutos) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.intervaloMinutos = intervaloMinutos;
    }

    public MedicoResponse(Long id, String nombreCompleto, String especialidad,
                          Integer intervaloMinutos, String username) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.intervaloMinutos = intervaloMinutos;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public Integer getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setIntervaloMinutos(Integer intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}