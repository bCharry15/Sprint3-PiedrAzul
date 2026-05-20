package co.edu.unicauca.piedraazul.service;

import co.edu.unicauca.piedraazul.model.User;

import java.util.List;

public interface IAgendadorService {

    /**
     * Registra un nuevo usuario con rol AGENDADOR.
     * Lanza IllegalArgumentException si el username ya existe.
     */
    User registrarAgendador(String username, String password);

    /**
     * Retorna todos los usuarios con rol AGENDADOR.
     */
    List<User> listarAgendadores();
}
