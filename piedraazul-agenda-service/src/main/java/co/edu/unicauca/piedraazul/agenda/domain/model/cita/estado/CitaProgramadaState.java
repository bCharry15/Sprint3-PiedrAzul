package co.edu.unicauca.piedraazul.agenda.domain.model.cita.estado;

import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;

public class CitaProgramadaState implements EstadoCitaState {

    @Override
    public EstadoCita obtenerEstado() {
        return EstadoCita.PROGRAMADA;
    }

    @Override
    public boolean puedeConfirmar() {
        return true;
    }

    @Override
    public boolean puedeAtender() {
        return false;
    }

    @Override
    public boolean puedeCancelar() {
        return true;
    }

    @Override
    public String descripcion() {
        return "La cita está programada y puede ser confirmada o cancelada.";
    }
}