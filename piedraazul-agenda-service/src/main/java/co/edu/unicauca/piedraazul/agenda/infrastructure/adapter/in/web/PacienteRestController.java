package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.web;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.application.usecase.DbiiSincronizacionService;
import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import co.edu.unicauca.piedraazul.agenda.model.enums.Genero;
import co.edu.unicauca.piedraazul.agenda.repository.PacienteRepository;

@RestController
public class PacienteRestController {

    private final PacienteRepository pacienteRepository;
    private final DbiiSincronizacionService dbiiSincronizacionService;

    public PacienteRestController(
            PacienteRepository pacienteRepository,
            DbiiSincronizacionService dbiiSincronizacionService
    ) {
        this.pacienteRepository = pacienteRepository;
        this.dbiiSincronizacionService = dbiiSincronizacionService;
    }

    @GetMapping("/api/pacientes/perfil/{username}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> obtenerPerfilPorUsername(@PathVariable String username) {
        validarTexto(username, "El username es obligatorio.");

        Paciente paciente = pacienteRepository.findByUsername(username.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un perfil de paciente asociado al usuario."
                ));

        return convertirPacienteAResponse(paciente);
    }

    @PutMapping("/api/pacientes/perfil")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> guardarPerfil(@RequestBody Map<String, Object> body) {
        String username = obtenerTexto(body, "username");
        String numeroDocumento = obtenerTexto(body, "numeroDocumento");
        String tipoDocumento = obtenerTexto(body, "tipoDocumento");
        String nombres = obtenerTexto(body, "nombres");
        String apellidos = obtenerTexto(body, "apellidos");
        String celular = obtenerTexto(body, "celular");
        String generoTexto = obtenerTexto(body, "genero");
        String fechaNacimientoTexto = obtenerTextoOpcional(body, "fechaNacimiento");
        String correo = obtenerTextoOpcional(body, "correo");

        validarTexto(username, "El username es obligatorio.");
        validarTexto(numeroDocumento, "El numero de documento es obligatorio.");
        validarTexto(tipoDocumento, "El tipo de documento es obligatorio.");
        validarTexto(nombres, "Los nombres son obligatorios.");
        validarTexto(apellidos, "Los apellidos son obligatorios.");
        validarTexto(celular, "El celular es obligatorio.");
        validarTexto(generoTexto, "El genero es obligatorio.");
        validarCelular(celular);

        username = username.trim();
        numeroDocumento = numeroDocumento.trim();

        Paciente pacientePorUsername = pacienteRepository.findByUsername(username)
                .orElse(null);

        Paciente pacientePorDocumento = pacienteRepository.findByNumeroDocumento(numeroDocumento)
                .orElse(null);

        if (pacientePorDocumento != null
                && pacientePorUsername != null
                && !pacientePorDocumento.getId().equals(pacientePorUsername.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El numero de documento ya esta asociado a otro perfil de paciente."
            );
        }

        if (pacientePorDocumento != null
                && pacientePorUsername == null
                && pacientePorDocumento.getUsername() != null
                && !pacientePorDocumento.getUsername().isBlank()
                && !pacientePorDocumento.getUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El numero de documento ya esta asociado a otro usuario."
            );
        }

        Paciente paciente = pacientePorUsername != null
                ? pacientePorUsername
                : pacientePorDocumento;

        if (paciente == null) {
            paciente = new Paciente();
        }

        paciente.setUsername(username);
        paciente.setNumeroDocumento(numeroDocumento);
        paciente.setTipoDocumento(normalizarTipoDocumento(tipoDocumento));
        paciente.setNombres(normalizarNombre(nombres));
        paciente.setApellidos(normalizarNombre(apellidos));
        paciente.setCelular(celular.trim());
        paciente.setGenero(convertirGenero(generoTexto));
        paciente.setFechaNacimiento(convertirFecha(fechaNacimientoTexto));
        paciente.setCorreo(normalizarCorreo(correo, username));

        Paciente guardado = pacienteRepository.save(paciente);

        dbiiSincronizacionService.sincronizarPacienteDesdePerfil(guardado);

        return convertirPacienteAResponse(guardado);
    }

    private Map<String, Object> convertirPacienteAResponse(Paciente paciente) {
        Map<String, Object> response = new HashMap<>();

        response.put("id", paciente.getId());
        response.put("username", paciente.getUsername());
        response.put("numeroDocumento", paciente.getNumeroDocumento());
        response.put("tipoDocumento", paciente.getTipoDocumento());
        response.put("nombres", paciente.getNombres());
        response.put("apellidos", paciente.getApellidos());
        response.put("celular", paciente.getCelular());
        response.put("genero", paciente.getGenero() != null ? paciente.getGenero().name() : null);
        response.put("fechaNacimiento", paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento().toString() : null);
        response.put("correo", paciente.getCorreo());

        return response;
    }

    private String obtenerTexto(Map<String, Object> body, String campo) {
        Object valor = body.get(campo);

        if (valor == null) {
            return "";
        }

        return valor.toString().trim();
    }

    private String obtenerTextoOpcional(Map<String, Object> body, String campo) {
        Object valor = body.get(campo);

        if (valor == null) {
            return null;
        }

        String texto = valor.toString().trim();

        return texto.isEmpty() ? null : texto;
    }

    private void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    private void validarCelular(String celular) {
        if (celular == null || !celular.trim().matches("\\d{10}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El celular debe contener exactamente 10 digitos numericos."
            );
        }
    }

    private Genero convertirGenero(String genero) {
        try {
            return Genero.valueOf(genero.trim().toUpperCase());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Genero invalido. Valores permitidos: HOMBRE, MUJER, OTRO."
            );
        }
    }

    private LocalDate convertirFecha(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) {
            return LocalDate.of(2000, 1, 1);
        }

        try {
            return LocalDate.parse(fecha.trim());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento debe tener formato yyyy-MM-dd."
            );
        }
    }

    private String normalizarTipoDocumento(String tipoDocumento) {
        if (tipoDocumento == null || tipoDocumento.isBlank()) {
            return "CC";
        }

        return switch (tipoDocumento.trim().toUpperCase()) {
            case "CEDULA DE CIUDADANIA", "CEDULA DE CIUDADANÍA", "CC" -> "CC";
            case "TARJETA DE IDENTIDAD", "TI" -> "TI";
            case "CEDULA DE EXTRANJERIA", "CEDULA DE EXTRANJERÍA", "CE" -> "CE";
            case "PASAPORTE" -> "PASAPORTE";
            default -> tipoDocumento.trim();
        };
    }

    private String normalizarNombre(String valor) {
        if (valor == null) {
            return "";
        }

        String limpio = valor.trim().replaceAll("\\s+", " ");

        if (limpio.isEmpty()) {
            return "";
        }

        String[] partes = limpio.toLowerCase().split(" ");
        StringBuilder resultado = new StringBuilder();

        for (String parte : partes) {
            if (parte.isBlank()) {
                continue;
            }

            resultado.append(Character.toUpperCase(parte.charAt(0)));

            if (parte.length() > 1) {
                resultado.append(parte.substring(1));
            }

            resultado.append(" ");
        }

        return resultado.toString().trim();
    }

    private String normalizarCorreo(String correo, String username) {
        if (correo != null && !correo.trim().isEmpty()) {
            return correo.trim().toLowerCase();
        }

        String usuarioLimpio = username == null
                ? "paciente"
                : username.toLowerCase().replaceAll("[^a-z0-9._-]", "");

        if (usuarioLimpio.isBlank()) {
            usuarioLimpio = "paciente";
        }

        return usuarioLimpio + "@piedraazul.local";
    }
}