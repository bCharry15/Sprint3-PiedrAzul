package co.edu.unicauca.piedraazul.agenda.model;

import co.edu.unicauca.piedraazul.agenda.observer.Observer;
import co.edu.unicauca.piedraazul.agenda.observer.Subject;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    // Composición: Subject es transitorio, no se persiste en BD
    @Transient
    private final Subject subject = new Subject();

    // ── Observer delegation ──────────────────────────────────────────────────

    public void attach(Observer observer) {
        subject.attach(observer);
    }

    public void detach(Observer observer) {
        subject.detach(observer);
    }

    public void notifyObservers(String message) {
        subject.notifyObservers(message);
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public UserStatus getStatus() { return status; }

    // Al cambiar el status notifica a los observers registrados
    public void setStatus(UserStatus status) {
        this.status = status;
        subject.notifyObservers(
                "Estado del usuario '" + this.username + "' cambió a: " + status);
    }
}

