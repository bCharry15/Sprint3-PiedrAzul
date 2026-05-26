package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.agenda.model.Cita;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;

@Service
public class DbiiSincronizacionService {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DbiiSincronizacionService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    public void sincronizarUsuarioSistema(String username, String clave, UserRole role) {
        validarTexto(username, "El username es obligatorio para sincronizar con USUARIO_SISTEMA.");
        validarTexto(clave, "La clave es obligatoria para sincronizar con USUARIO_SISTEMA.");

        if (role == null) {
            throw new IllegalArgumentException("El rol es obligatorio para sincronizar con USUARIO_SISTEMA.");
        }

        asegurarRolesBase();

        String usernameNormalizado = username.trim();
        String claveNormalizada = limitarTexto(clave.trim(), 100);
        int idRol = obtenerIdRolDbii(role);

        Long cantidadExistente = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM USUARIO_SISTEMA
                WHERE LOWER(NOMBRE_USUARIO) = LOWER(?)
                """,
                Long.class,
                usernameNormalizado
        );

        Long idUsuario;

        if (cantidadExistente != null && cantidadExistente > 0) {
            idUsuario = jdbcTemplate.queryForObject(
                    """
                    SELECT ID_USUARIO
                    FROM USUARIO_SISTEMA
                    WHERE LOWER(NOMBRE_USUARIO) = LOWER(?)
                    FETCH FIRST 1 ROWS ONLY
                    """,
                    Long.class,
                    usernameNormalizado
            );

            jdbcTemplate.update(
                    """
                    UPDATE USUARIO_SISTEMA
                    SET CLAVE = ?,
                        NOMBRE_COMPLETO = ?,
                        EMAIL = ?,
                        ESTADO = 'A',
                        ID_ROL = ?
                    WHERE ID_USUARIO = ?
                    """,
                    claveNormalizada,
                    construirNombreCompletoUsuario(usernameNormalizado, role),
                    construirEmailAcademico(usernameNormalizado),
                    idRol,
                    idUsuario
            );

            registrarAuditoriaUsuario(
                    idUsuario,
                    "SINCRONIZAR_USUARIO",
                    "Usuario actualizado desde la aplicacion principal: " + usernameNormalizado
            );

        } else {
            idUsuario = obtenerSiguienteIdUsuarioSistema();

            jdbcTemplate.update(
                    """
                    INSERT INTO USUARIO_SISTEMA (
                        ID_USUARIO,
                        NOMBRE_USUARIO,
                        CLAVE,
                        NOMBRE_COMPLETO,
                        EMAIL,
                        ESTADO,
                        ID_ROL
                    )
                    VALUES (?, ?, ?, ?, ?, 'A', ?)
                    """,
                    idUsuario,
                    usernameNormalizado,
                    claveNormalizada,
                    construirNombreCompletoUsuario(usernameNormalizado, role),
                    construirEmailAcademico(usernameNormalizado),
                    idRol
            );

            registrarAuditoriaUsuario(
                    idUsuario,
                    "CREAR_USUARIO",
                    "Usuario creado desde la aplicacion principal: " + usernameNormalizado
            );
        }
    }

    public void sincronizarMedicoTerapeuta(Medico medico) {
        if (medico == null) {
            throw new IllegalArgumentException("El medico es obligatorio para sincronizar con MEDICO_TERAPISTA.");
        }

        User user = medico.getUser();

        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("El medico debe tener usuario asociado para sincronizar con MEDICO_TERAPISTA.");
        }

        String username = user.getUsername().trim();
        String documento = generarDocumentoMedico(username);
        String email = construirEmailAcademico(username);
        String telefono = "3000000000";
        String tipoProfesional = limitarTexto(medico.getEspecialidad(), 50);
        Integer intervalo = medico.getIntervaloMinutos() != null ? medico.getIntervaloMinutos() : 15;

        NombreDividido nombreDividido = dividirNombreCompleto(medico.getNombreCompleto(), username);

        Long cantidadExistente = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM MEDICO_TERAPISTA
                WHERE NUM_DOCUMENTO = ?
                   OR LOWER(EMAIL) = LOWER(?)
                """,
                Long.class,
                documento,
                email
        );

        Long idUsuarioSistema = obtenerIdUsuarioSistema(username);

        if (cantidadExistente != null && cantidadExistente > 0) {
            Long idMedicoDbii = obtenerIdMedicoDbiiPorDocumentoOEmail(documento, email);

            jdbcTemplate.update(
                    """
                    UPDATE MEDICO_TERAPISTA
                    SET TIPO_DOCUMENTO = 'CC',
                        NUM_DOCUMENTO = ?,
                        NOMBRES = ?,
                        APELLIDOS = ?,
                        TIPO_PROFESIONAL = ?,
                        INTERVALO_MINUTOS = ?,
                        TELEFONO = ?,
                        EMAIL = ?,
                        ESTADO = 'A'
                    WHERE ID_MEDICO = ?
                    """,
                    documento,
                    nombreDividido.nombres(),
                    nombreDividido.apellidos(),
                    tipoProfesional,
                    intervalo,
                    telefono,
                    email,
                    idMedicoDbii
            );

            registrarAuditoriaUsuario(
                    idUsuarioSistema,
                    "SINCRONIZAR_MEDICO",
                    "Medico actualizado desde la aplicacion principal: " + medico.getNombreCompleto()
            );

            System.out.println("DBII-SYNC -> Medico actualizado en MEDICO_TERAPISTA. ID: " + idMedicoDbii);
            return;
        }

        Long idMedicoGenerado = insertarMedicoConProcedimiento(
                documento,
                nombreDividido.nombres(),
                nombreDividido.apellidos(),
                tipoProfesional,
                intervalo,
                telefono,
                email
        );

        registrarAuditoriaUsuario(
                idUsuarioSistema,
                "CREAR_MEDICO",
                "Medico creado desde la aplicacion principal: " + medico.getNombreCompleto()
        );

        System.out.println("DBII-SYNC -> Medico creado en MEDICO_TERAPISTA usando PKG_GESTION_MEDICOS.INSERTAR_MEDICO. ID: "
                + idMedicoGenerado);
    }

    public void desactivarMedicoTerapeuta(Medico medico) {
        if (medico == null || medico.getUser() == null || medico.getUser().getUsername() == null) {
            return;
        }

        String username = medico.getUser().getUsername().trim();
        String documento = generarDocumentoMedico(username);
        String email = construirEmailAcademico(username);

        int filas = jdbcTemplate.update(
                """
                UPDATE MEDICO_TERAPISTA
                SET ESTADO = 'I'
                WHERE NUM_DOCUMENTO = ?
                   OR LOWER(EMAIL) = LOWER(?)
                """,
                documento,
                email
        );

        if (filas > 0) {
            registrarAuditoriaUsuario(
                    obtenerIdUsuarioSistema(username),
                    "DESACTIVAR_MEDICO",
                    "Medico desactivado desde la aplicacion principal: " + medico.getNombreCompleto()
            );
        }
    }

    public void sincronizarPacienteYCita(Cita cita) {
        if (cita == null) {
            throw new IllegalArgumentException("La cita es obligatoria para sincronizar con CITA DBII.");
        }

        if (cita.getPaciente() == null) {
            throw new IllegalArgumentException("La cita debe tener paciente para sincronizar con PACIENTE DBII.");
        }

        if (cita.getMedico() == null) {
            throw new IllegalArgumentException("La cita debe tener medico para sincronizar con CITA DBII.");
        }

        Long idPacienteDbii = sincronizarPaciente(cita.getPaciente());
        Long idMedicoDbii = obtenerOcrearMedicoDbii(cita.getMedico());

        Long idCitaExistente = buscarCitaDbiiExistente(
                idPacienteDbii,
                idMedicoDbii,
                cita
        );

        if (idCitaExistente != null) {
            registrarAuditoriaUsuario(
                    obtenerUsuarioAuditoriaParaCita(cita),
                    "SINCRONIZAR_CITA",
                    "Cita ya existia en DBII. ID_CITA: " + idCitaExistente
            );

            System.out.println("DBII-SYNC -> Cita ya existia en CITA DBII. ID: " + idCitaExistente);
            return;
        }

        Long idCitaGenerada = insertarCitaConProcedimiento(
                idPacienteDbii,
                idMedicoDbii,
                cita
        );

        registrarAuditoriaUsuario(
                obtenerUsuarioAuditoriaParaCita(cita),
                "CREAR_CITA",
                "Cita creada desde la aplicacion principal. ID_CITA_DBII: " + idCitaGenerada
        );

        System.out.println("DBII-SYNC -> Cita creada en CITA usando PKG_GESTION_CITAS.INSERTAR_CITA. ID: "
                + idCitaGenerada);
    }

    private Long sincronizarPaciente(Paciente paciente) {
        validarTexto(paciente.getNumeroDocumento(), "El numero de documento del paciente es obligatorio.");
        validarTexto(paciente.getTipoDocumento(), "El tipo de documento del paciente es obligatorio.");
        validarTexto(paciente.getNombres(), "Los nombres del paciente son obligatorios.");
        validarTexto(paciente.getApellidos(), "Los apellidos del paciente son obligatorios.");

        String documento = limitarTexto(paciente.getNumeroDocumento(), 20);
        String tipoDocumento = limitarTexto(paciente.getTipoDocumento(), 20);
        String nombres = limitarTexto(paciente.getNombres(), 60);
        String apellidos = limitarTexto(paciente.getApellidos(), 60);
        LocalDate fechaNacimiento = paciente.getFechaNacimiento() != null
                ? paciente.getFechaNacimiento()
                : LocalDate.of(2000, 1, 1);
        String celular = limitarTexto(paciente.getCelular() != null ? paciente.getCelular() : "3000000000", 15);
        String direccion = "Direccion registrada desde app principal";
        String email = limitarTexto(
                paciente.getCorreo() != null && !paciente.getCorreo().isBlank()
                        ? paciente.getCorreo()
                        : construirEmailPaciente(documento),
                100
        );

        Long cantidadExistente = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM PACIENTE
                WHERE NUM_DOCUMENTO = ?
                """,
                Long.class,
                documento
        );

        if (cantidadExistente != null && cantidadExistente > 0) {
            Long idPacienteDbii = obtenerIdPacienteDbiiPorDocumento(documento);

            jdbcTemplate.update(
                    """
                    UPDATE PACIENTE
                    SET TIPO_DOCUMENTO = ?,
                        NOMBRES = ?,
                        APELLIDOS = ?,
                        FECHA_NACIMIENTO = ?,
                        CELULAR = ?,
                        DIRECCION = ?,
                        EMAIL = ?,
                        ESTADO = 'A'
                    WHERE ID_PACIENTE = ?
                    """,
                    tipoDocumento,
                    nombres,
                    apellidos,
                    Date.valueOf(fechaNacimiento),
                    celular,
                    direccion,
                    email,
                    idPacienteDbii
            );

            System.out.println("DBII-SYNC -> Paciente actualizado en PACIENTE. ID: " + idPacienteDbii);
            return idPacienteDbii;
        }

        Long idPacienteGenerado = insertarPacienteConProcedimiento(
                tipoDocumento,
                documento,
                nombres,
                apellidos,
                fechaNacimiento,
                celular,
                direccion,
                email
        );

        System.out.println("DBII-SYNC -> Paciente creado en PACIENTE usando PKG_GESTION_PACIENTES.INSERTAR_PACIENTE. ID: "
                + idPacienteGenerado);

        return idPacienteGenerado;
    }

    private Long obtenerOcrearMedicoDbii(Medico medico) {
        if (medico.getUser() != null && medico.getUser().getUsername() != null) {
            String username = medico.getUser().getUsername().trim();
            String documento = generarDocumentoMedico(username);
            String email = construirEmailAcademico(username);

            Long idMedico = obtenerIdMedicoDbiiPorDocumentoOEmail(documento, email);

            if (idMedico != null) {
                return idMedico;
            }
        }

        sincronizarMedicoTerapeuta(medico);

        if (medico.getUser() != null && medico.getUser().getUsername() != null) {
            String username = medico.getUser().getUsername().trim();
            String documento = generarDocumentoMedico(username);
            String email = construirEmailAcademico(username);

            Long idMedico = obtenerIdMedicoDbiiPorDocumentoOEmail(documento, email);

            if (idMedico != null) {
                return idMedico;
            }
        }

        throw new IllegalStateException("No fue posible obtener o crear el medico en MEDICO_TERAPISTA.");
    }

    private Long buscarCitaDbiiExistente(Long idPacienteDbii, Long idMedicoDbii, Cita cita) {
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT ID_CITA
                    FROM CITA
                    WHERE ID_PACIENTE = ?
                      AND ID_MEDICO = ?
                      AND TRUNC(FECHA_CITA) = ?
                      AND HORA_CITA = ?
                    FETCH FIRST 1 ROWS ONLY
                    """,
                    Long.class,
                    idPacienteDbii,
                    idMedicoDbii,
                    Date.valueOf(cita.getFecha()),
                    formatearHora(cita)
            );
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private Long insertarPacienteConProcedimiento(
            String tipoDocumento,
            String numDocumento,
            String nombres,
            String apellidos,
            LocalDate fechaNacimiento,
            String celular,
            String direccion,
            String email
    ) {
        String llamada = "{call PKG_GESTION_PACIENTES.INSERTAR_PACIENTE(?,?,?,?,?,?,?,?,?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement cs = connection.prepareCall(llamada)
        ) {
            cs.setString(1, limitarTexto(tipoDocumento, 20));
            cs.setString(2, limitarTexto(numDocumento, 20));
            cs.setString(3, limitarTexto(nombres, 60));
            cs.setString(4, limitarTexto(apellidos, 60));
            cs.setDate(5, Date.valueOf(fechaNacimiento));
            cs.setString(6, limitarTexto(celular, 15));
            cs.setString(7, limitarTexto(direccion, 120));
            cs.setString(8, limitarTexto(email, 100));
            cs.registerOutParameter(9, Types.NUMERIC);

            cs.execute();

            Number id = (Number) cs.getObject(9);

            if (id == null) {
                throw new IllegalStateException("El procedimiento PKG_GESTION_PACIENTES.INSERTAR_PACIENTE no retorno ID.");
            }

            return id.longValue();

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Error ejecutando PKG_GESTION_PACIENTES.INSERTAR_PACIENTE: " + ex.getMessage(),
                    ex
            );
        }
    }

    private Long insertarCitaConProcedimiento(Long idPacienteDbii, Long idMedicoDbii, Cita cita) {
        String llamada = "{call PKG_GESTION_CITAS.INSERTAR_CITA(?,?,?,?,?,?,?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement cs = connection.prepareCall(llamada)
        ) {
            cs.setLong(1, idPacienteDbii);
            cs.setLong(2, idMedicoDbii);
            cs.setDate(3, Date.valueOf(cita.getFecha()));
            cs.setString(4, formatearHora(cita));
            cs.setString(5, "Agendamiento desde app principal");
            cs.setString(6, limitarTexto(
                    cita.getObservacion() != null ? cita.getObservacion() : "Cita sincronizada desde PiedraAzul",
                    200
            ));
            cs.registerOutParameter(7, Types.NUMERIC);

            cs.execute();

            Number id = (Number) cs.getObject(7);

            if (id == null) {
                throw new IllegalStateException("El procedimiento PKG_GESTION_CITAS.INSERTAR_CITA no retorno ID.");
            }

            return id.longValue();

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Error ejecutando PKG_GESTION_CITAS.INSERTAR_CITA: " + ex.getMessage(),
                    ex
            );
        }
    }

    private Long insertarMedicoConProcedimiento(
            String numDocumento,
            String nombres,
            String apellidos,
            String tipoProfesional,
            Integer intervaloMinutos,
            String telefono,
            String email
    ) {
        String llamada = "{call PKG_GESTION_MEDICOS.INSERTAR_MEDICO(?,?,?,?,?,?,?,?,?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement cs = connection.prepareCall(llamada)
        ) {
            cs.setString(1, "CC");
            cs.setString(2, limitarTexto(numDocumento, 20));
            cs.setString(3, limitarTexto(nombres, 60));
            cs.setString(4, limitarTexto(apellidos, 60));
            cs.setString(5, limitarTexto(tipoProfesional, 50));
            cs.setInt(6, intervaloMinutos);
            cs.setString(7, limitarTexto(telefono, 15));
            cs.setString(8, limitarTexto(email, 100));
            cs.registerOutParameter(9, Types.NUMERIC);

            cs.execute();

            Number id = (Number) cs.getObject(9);

            if (id == null) {
                throw new IllegalStateException("El procedimiento PKG_GESTION_MEDICOS.INSERTAR_MEDICO no retorno ID.");
            }

            return id.longValue();

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Error ejecutando PKG_GESTION_MEDICOS.INSERTAR_MEDICO: " + ex.getMessage(),
                    ex
            );
        }
    }

    private Long obtenerIdPacienteDbiiPorDocumento(String documento) {
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT ID_PACIENTE
                    FROM PACIENTE
                    WHERE NUM_DOCUMENTO = ?
                    FETCH FIRST 1 ROWS ONLY
                    """,
                    Long.class,
                    documento
            );
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private Long obtenerIdMedicoDbiiPorDocumentoOEmail(String documento, String email) {
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT ID_MEDICO
                    FROM MEDICO_TERAPISTA
                    WHERE NUM_DOCUMENTO = ?
                       OR LOWER(EMAIL) = LOWER(?)
                    FETCH FIRST 1 ROWS ONLY
                    """,
                    Long.class,
                    documento,
                    email
            );
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private Long obtenerUsuarioAuditoriaParaCita(Cita cita) {
        if (cita == null || cita.getMedico() == null || cita.getMedico().getUser() == null) {
            return null;
        }

        return obtenerIdUsuarioSistema(cita.getMedico().getUser().getUsername());
    }

    private String formatearHora(Cita cita) {
        if (cita == null || cita.getHora() == null) {
            return "08:00";
        }

        return cita.getHora().format(FORMATO_HORA);
    }

    private void asegurarRolesBase() {
        asegurarRol(1, "ADMINISTRADOR", "Administra todo el sistema");
        asegurarRol(2, "AGENDADOR", "Gestiona citas y pacientes");
        asegurarRol(3, "MEDICO", "Atiende pacientes y registra historia clinica");
        asegurarRol(4, "PACIENTE", "Paciente con acceso al agendamiento autonomo");
    }

    private void asegurarRol(int idRol, String nombre, String descripcion) {
        jdbcTemplate.update(
                """
                MERGE INTO ROL r
                USING (
                    SELECT ? AS ID_ROL,
                           ? AS NOMBRE,
                           ? AS DESCRIPCION
                    FROM DUAL
                ) datos
                ON (r.ID_ROL = datos.ID_ROL)
                WHEN MATCHED THEN
                    UPDATE SET
                        r.NOMBRE = datos.NOMBRE,
                        r.DESCRIPCION = datos.DESCRIPCION,
                        r.ESTADO = 'A'
                WHEN NOT MATCHED THEN
                    INSERT (
                        ID_ROL,
                        NOMBRE,
                        DESCRIPCION,
                        ESTADO
                    )
                    VALUES (
                        datos.ID_ROL,
                        datos.NOMBRE,
                        datos.DESCRIPCION,
                        'A'
                    )
                """,
                idRol,
                nombre,
                descripcion
        );
    }

    private int obtenerIdRolDbii(UserRole role) {
        return switch (role) {
            case ADMIN -> 1;
            case AGENDADOR -> 2;
            case MEDICO -> 3;
            case PACIENTE -> 4;
        };
    }

    private Long obtenerSiguienteIdUsuarioSistema() {
        Long siguienteId = jdbcTemplate.queryForObject(
                """
                SELECT NVL(MAX(ID_USUARIO), 0) + 1
                FROM USUARIO_SISTEMA
                """,
                Long.class
        );

        return siguienteId != null ? siguienteId : 1L;
    }

    private Long obtenerIdUsuarioSistema(String username) {
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT ID_USUARIO
                    FROM USUARIO_SISTEMA
                    WHERE LOWER(NOMBRE_USUARIO) = LOWER(?)
                    FETCH FIRST 1 ROWS ONLY
                    """,
                    Long.class,
                    username
            );
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private void registrarAuditoriaUsuario(Long idUsuario, String accion, String descripcion) {
        if (idUsuario == null) {
            return;
        }

        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO AUDITORIA (
                        ID_AUDITORIA,
                        ID_USUARIO,
                        FECHA_EVENTO,
                        ACCION,
                        TABLA_AFECTADA,
                        DESCRIPCION
                    )
                    VALUES (
                        SEQ_AUDITORIA.NEXTVAL,
                        ?,
                        SYSDATE,
                        ?,
                        ?,
                        ?
                    )
                    """,
                    idUsuario,
                    limitarTexto(accion, 50),
                    obtenerTablaAfectadaPorAccion(accion),
                    limitarTexto(descripcion, 200)
            );
        } catch (DataAccessException ex) {
            System.out.println("DBII-SYNC -> No se pudo registrar auditoria: " + ex.getMessage());
        }
    }

    private String obtenerTablaAfectadaPorAccion(String accion) {
        if (accion == null) {
            return "DBII";
        }

        if (accion.contains("MEDICO")) {
            return "MEDICO_TERAPISTA";
        }

        if (accion.contains("USUARIO")) {
            return "USUARIO_SISTEMA";
        }

        if (accion.contains("CITA")) {
            return "CITA";
        }

        if (accion.contains("PACIENTE")) {
            return "PACIENTE";
        }

        return "DBII";
    }

    private String construirNombreCompletoUsuario(String username, UserRole role) {
        String rolLegible = switch (role) {
            case ADMIN -> "Administrador";
            case AGENDADOR -> "Agendador";
            case MEDICO -> "Medico";
            case PACIENTE -> "Paciente";
        };

        return limitarTexto(rolLegible + " " + username, 100);
    }

    private String construirEmailAcademico(String username) {
        String usuarioLimpio = username
                .toLowerCase()
                .replaceAll("[^a-z0-9._-]", "");

        if (usuarioLimpio.isBlank()) {
            usuarioLimpio = "usuario";
        }

        return limitarTexto(usuarioLimpio + "@piedraazul.local", 100);
    }

    private String construirEmailPaciente(String documento) {
        String documentoLimpio = documento
                .toLowerCase()
                .replaceAll("[^a-z0-9._-]", "");

        if (documentoLimpio.isBlank()) {
            documentoLimpio = "paciente";
        }

        return limitarTexto("paciente." + documentoLimpio + "@piedraazul.local", 100);
    }

    private String generarDocumentoMedico(String username) {
        long hash = Math.abs((long) username.toLowerCase().hashCode());
        return limitarTexto("MED" + hash, 20);
    }

    private NombreDividido dividirNombreCompleto(String nombreCompleto, String usernameFallback) {
        String nombre = nombreCompleto == null || nombreCompleto.trim().isEmpty()
                ? usernameFallback
                : nombreCompleto.trim();

        String[] partes = nombre.split("\\s+");

        if (partes.length == 1) {
            return new NombreDividido(
                    limitarTexto(partes[0], 60),
                    limitarTexto(usernameFallback, 60)
            );
        }

        StringBuilder nombres = new StringBuilder();

        for (int i = 0; i < partes.length - 1; i++) {
            if (!nombres.isEmpty()) {
                nombres.append(" ");
            }

            nombres.append(partes[i]);
        }

        String apellidos = partes[partes.length - 1];

        return new NombreDividido(
                limitarTexto(nombres.toString(), 60),
                limitarTexto(apellidos, 60)
        );
    }

    private String limitarTexto(String valor, int longitudMaxima) {
        if (valor == null) {
            return "";
        }

        String valorLimpio = valor.trim();

        if (valorLimpio.length() <= longitudMaxima) {
            return valorLimpio;
        }

        return valorLimpio.substring(0, longitudMaxima);
    }

    private void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private record NombreDividido(String nombres, String apellidos) {
    }
}