package co.edu.unicauca.piedraazul.service;

import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.observer.Observer;

public interface IUserService {

    /**
     * Suscribe un observer para recibir eventos de autenticación y registro.
     */
    void attach(Observer observer);

    /**
     * Elimina un observer previamente suscrito.
     */
    void detach(Observer observer);

    /**
     * Registra un nuevo usuario notificando a un observer (por ejemplo, la vista).
     * Retorna true si el registro fue exitoso, false si el usuario ya existe.
     */
    boolean registerUser(User user, Observer vista);

    /**
     * Registra un usuario sin observer asociado.
     */
    boolean registerUser(User user);

    /**
     * Autentica un usuario con su username y contraseña en texto plano.
     * Retorna el User si las credenciales son válidas, null en caso contrario.
     */
    User authenticate(String username, String rawPassword);
}
