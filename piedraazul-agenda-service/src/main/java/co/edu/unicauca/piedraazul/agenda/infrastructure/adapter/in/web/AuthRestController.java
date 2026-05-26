package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.web;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.GestionarUsuariosUseCase;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final GestionarUsuariosUseCase gestionarUsuariosUseCase;

    public AuthRestController(GestionarUsuariosUseCase gestionarUsuariosUseCase) {
        this.gestionarUsuariosUseCase = gestionarUsuariosUseCase;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        return gestionarUsuariosUseCase.login(
                body.get("username"),
                body.get("password")
        );
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> body) {
        return gestionarUsuariosUseCase.registrar(
                body.get("username"),
                body.get("password"),
                body.get("role")
        );
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> body) {
        return gestionarUsuariosUseCase.generarPasswordTemporal(
                body.get("username")
        );
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> body) {
        return gestionarUsuariosUseCase.restablecerPasswordSeguro(
                body.get("username"),
                body.get("numeroDocumento"),
                body.get("nuevaPassword")
        );
    }
}