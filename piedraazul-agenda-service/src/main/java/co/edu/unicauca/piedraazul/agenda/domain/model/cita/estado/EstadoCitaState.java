package co.edu.unicauca.piedraazul.agenda.domain.model.cita.estado;

import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;

public interface EstadoCitaState {

    EstadoCita obtenerEstado();

    boolean puedeConfirmar();

    boolean puedeAtender();

    boolean puedeCancelar();

    String descripcion();
}