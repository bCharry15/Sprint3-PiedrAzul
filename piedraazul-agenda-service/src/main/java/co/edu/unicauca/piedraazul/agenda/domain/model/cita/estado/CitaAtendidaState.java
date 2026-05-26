package co.edu.unicauca.piedraazul.agenda.domain.model.cita.estado;

import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;

public class CitaAtendidaState implements EstadoCitaState {

    @Override
    public EstadoCita obtenerEstado() {
        return EstadoCita.ATENDIDA;
    }

    @Override
    public boolean puedeConfirmar() {
        return false;
    }

    @Override
    public boolean puedeAtender() {
        return false;
    }

    @Override
    public boolean puedeCancelar() {
        return false;
    }

    @Override
    public String descripcion() {
        return "La cita ya fue atendida y no permite cambios de estado.";
    }
}