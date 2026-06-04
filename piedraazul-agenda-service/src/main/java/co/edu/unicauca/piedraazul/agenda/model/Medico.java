package co.edu.unicauca.piedraazul.agenda.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicos")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombreCompleto;

    @Column(nullable = false, length = 100)
    private String especialidad;

    @Column(nullable = false)
    private Integer intervaloMinutos;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    public Medico() {
    }

    @PrePersist
    public void prePersist() {
        if (activo == null) {
            activo = true;
        }
    }

    public Long getId() {
        return id;
    }

    /*
     * Este setter ayuda cuando el objeto se construye desde DTOs o pruebas.
     * En JPA normalmente el id lo asigna la base de datos.
     */
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public boolean estaActivo() {
        return Boolean.TRUE.equals(activo);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return nombreCompleto + " - " + especialidad;
    }
}