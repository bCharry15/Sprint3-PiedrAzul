package co.edu.unicauca.piedraazul.util;

public enum Vista {

    // ── Autenticación ─────────────────────────────────────────────────────────
    LOGIN("auth/login.xml"),
    REGISTER("auth/register.xml"),

    // ── Paneles ───────────────────────────────────────────────────────────────
    ADMIN_PANEL("panels/adminPanel.xml"),
    AGENDADOR_PANEL("panels/agendadorPanel.xml"),
    MEDICO_PANEL("panels/medicoPanel.xml"),
    PACIENTE_PANEL("panels/pacientePanel.xml"),
    USER_PANEL("panels/userPanel.xml");

    private final String ruta;

    Vista(String ruta) {
        this.ruta = ruta;
    }

    public String getRuta() {
        return ruta;
    }
}
