package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.GestionarUsuariosUseCase;

@RestController
public class AuthRestController {

    private final GestionarUsuariosUseCase gestionarUsuariosUseCase;

    public AuthRestController(GestionarUsuariosUseCase gestionarUsuariosUseCase) {
        this.gestionarUsuariosUseCase = gestionarUsuariosUseCase;
    }

    @PostMapping("/api/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        return gestionarUsuariosUseCase.login(
                body.get("username"),
                body.get("password")
        );
    }

    @PostMapping("/api/auth/register")
    public Map<String, String> register(@RequestBody Map<String, String> body) {
        return gestionarUsuariosUseCase.registrar(
                body.get("username"),
                body.get("password"),
                body.get("role")
        );
    }

    @PostMapping("/api/auth/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> body) {
        return gestionarUsuariosUseCase.generarPasswordTemporal(
                body.get("username")
        );
    }

    @PostMapping("/api/auth/reset-password")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> body) {
        return gestionarUsuariosUseCase.restablecerPasswordSeguro(
                body.get("username"),
                body.get("numeroDocumento"),
                body.get("nuevaPassword")
        );
    }

    @GetMapping("/api/auth/users/role/{role}")
    public List<Map<String, Object>> listarUsuariosPorRol(@PathVariable String role) {
        return gestionarUsuariosUseCase.listarUsuariosPorRol(role);
    }
}