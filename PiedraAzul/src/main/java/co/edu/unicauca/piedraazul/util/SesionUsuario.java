package co.edu.unicauca.piedraazul.util;

import co.edu.unicauca.piedraazul.model.User;

public class SesionUsuario {

    private static User usuarioActual;

    private SesionUsuario() {
    }

    public static User getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(User usuario) {
        usuarioActual = usuario;
    }

    public static void limpiarSesion() {
        usuarioActual = null;
    }

    public static void clear() {
        limpiarSesion();
    }

    public static String getUsernameActual() {
        if (usuarioActual == null) {
            return null;
        }

        return usuarioActual.getUsername();
    }
}