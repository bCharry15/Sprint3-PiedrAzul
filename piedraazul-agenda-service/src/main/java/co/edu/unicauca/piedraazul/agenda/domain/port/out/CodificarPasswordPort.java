package co.edu.unicauca.piedraazul.agenda.domain.port.out;

public interface CodificarPasswordPort {

    String codificar(String passwordPlano);

    boolean coincide(String passwordPlano, String passwordCodificado);
}