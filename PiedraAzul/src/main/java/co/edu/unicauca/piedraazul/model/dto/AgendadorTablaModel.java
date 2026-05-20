package co.edu.unicauca.piedraazul.model.dto;

public class AgendadorTablaModel {

    private final Long id;
    private final String username;
    private final String status;
    private final String role;

    public AgendadorTablaModel(Long id, String username, String status, String role) {
        this.id = id;
        this.username = username;
        this.status = status;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public String getRole() {
        return role;
    }
}