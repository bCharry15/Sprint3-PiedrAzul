package co.edu.unicauca.piedraazul.agenda.domain.model.cita.estado;

import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;

public class CitaConfirmadaState implements EstadoCitaState {

    @Override
    public EstadoCita obtenerEstado() {
        return EstadoCita.CONFIRMADA;
    }

    @Override
    public boolean puedeConfirmar() {
        return false;
    }

    @Override
    public boolean puedeAtender() {
        return true;
    }

    @Override
    public boolean puedeCancelar() {
        return true;
    }

    @Override
    public String descripcion() {
        return "La cita está confirmada y puede ser atendida o cancelada.";
    }
}