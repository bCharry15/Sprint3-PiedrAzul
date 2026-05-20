package co.edu.unicauca.piedraazul.model;

import co.edu.unicauca.piedraazul.model.enums.UserRole;
import co.edu.unicauca.piedraazul.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.observer.Observer;
import co.edu.unicauca.piedraazul.observer.Subject;

public class User {

    private Long id;
    private String username;
    private String password;
    private UserRole role;
    private UserStatus status;

    private final Subject subject = new Subject();

    public void attach(Observer observer) {
        subject.attach(observer);
    }

    public void detach(Observer observer) {
        subject.detach(observer);
    }

    public void notifyObservers(String message) {
        subject.notifyObservers(message);
    }

    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getUsername() { 
        return username; 
    }

    public void setUsername(String username) { 
        this.username = username; 
    }

    public String getPassword() { 
        return password; 
    }

    public void setPassword(String password) { 
        this.password = password; 
    }

    public UserRole getRole() { 
        return role; 
    }

    public void setRole(UserRole role) { 
        this.role = role; 
    }

    public UserStatus getStatus() { 
        return status; 
    }

    public void setStatus(UserStatus status) {
        this.status = status;
        subject.notifyObservers(
                "Estado del usuario '" + this.username + "' cambió a: " + status);
    }
}