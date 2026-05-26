package co.edu.unicauca.piedraazul;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DbiiOraclePanelApp extends Application {

    private TextField baseUrlField;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField documentoField;
    private Label estadoLabel;
    private TextArea salidaArea;

    private String token;

    @Override
    public void start(Stage stage) {
        baseUrlField = new TextField("http://127.0.0.1:8081");
        usernameField = new TextField("admin");
        passwordField = new PasswordField();
        passwordField.setText("admin123");

        documentoField = new TextField("777123456");
        documentoField.setPromptText("Documento del paciente. Ejemplo: 777123456");

        estadoLabel = new Label("Sin autenticar");
        estadoLabel.setStyle("-fx-text-fill: #9a3412; -fx-font-weight: bold;");

        salidaArea = new TextArea();
        salidaArea.setEditable(false);
        salidaArea.setWrapText(true);
        salidaArea.setStyle("""
                -fx-font-family: Consolas;
                -fx-font-size: 13px;
                """);

        VBox root = new VBox(16);
        root.setPadding(new Insets(22));
        root.setStyle("""
                -fx-background-color: #eef4fb;
                """);

        Label titulo = new Label("Panel DBII Oracle - PiedraAzul");
        titulo.setStyle("""
                -fx-font-size: 26px;
                -fx-font-weight: bold;
                -fx-text-fill: #0f172a;
                """);

        Label subtitulo = new Label("Evidencia de diccionario de datos, objetos PL/SQL, vistas, procedimientos y datos creados desde la app.");
        subtitulo.setStyle("""
                -fx-font-size: 14px;
                -fx-text-fill: #475569;
                """);

        VBox encabezado = new VBox(4, titulo, subtitulo);

        GridPane formulario = crearFormularioConexion();

        HBox filaAccionesPrincipales = crearFilaAccionesPrincipales();
        HBox filaAccionesDbii = crearFilaAccionesDbii();
        HBox filaAccionesReportes = crearFilaAccionesReportes();
        HBox filaConsultaDocumento = crearFilaConsultaDocumento();

        Label salidaLabel = new Label("Salida / Evidencia");
        salidaLabel.setStyle("""
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-text-fill: #0f172a;
                """);

        ScrollPane scrollSalida = new ScrollPane(salidaArea);
        scrollSalida.setFitToWidth(true);
        scrollSalida.setFitToHeight(true);
        VBox.setVgrow(scrollSalida, Priority.ALWAYS);

        root.getChildren().addAll(
                encabezado,
                formulario,
                filaAccionesPrincipales,
                filaAccionesDbii,
                filaAccionesReportes,
                filaConsultaDocumento,
                salidaLabel,
                scrollSalida
        );

        Scene scene = new Scene(root, 1350, 820);
        stage.setTitle("PiedraAzul - Panel DBII Oracle");
        stage.setScene(scene);
        stage.show();
    }

    private GridPane crearFormularioConexion() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 16;
                -fx-border-radius: 16;
                -fx-border-color: #dbeafe;
                """);

        Label baseUrlLabel = crearLabelCampo("Agenda Service URL");
        Label usernameLabel = crearLabelCampo("Usuario");
        Label passwordLabel = crearLabelCampo("Contrasena");
        Label estadoTituloLabel = crearLabelCampo("Estado");
        Label documentoLabel = crearLabelCampo("Documento evidencia");

        grid.add(baseUrlLabel, 0, 0);
        grid.add(baseUrlField, 1, 0);

        grid.add(estadoTituloLabel, 2, 0);
        grid.add(estadoLabel, 3, 0);

        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);

        grid.add(passwordLabel, 2, 1);
        grid.add(passwordField, 3, 1);

        grid.add(documentoLabel, 0, 2);
        grid.add(documentoField, 1, 2, 3, 1);

        GridPane.setHgrow(baseUrlField, Priority.ALWAYS);
        GridPane.setHgrow(usernameField, Priority.ALWAYS);
        GridPane.setHgrow(passwordField, Priority.ALWAYS);
        GridPane.setHgrow(documentoField, Priority.ALWAYS);

        return grid;
    }

    private HBox crearFilaAccionesPrincipales() {
        Button loginButton = crearBotonPrimario("Autenticar admin");
        loginButton.setOnAction(event -> autenticar());

        Button limpiarButton = crearBotonSecundario("Limpiar salida");
        limpiarButton.setOnAction(event -> salidaArea.clear());

        HBox fila = new HBox(10, loginButton, limpiarButton);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private HBox crearFilaAccionesDbii() {
        Button healthButton = crearBoton("Health DBII");
        healthButton.setOnAction(event -> ejecutarGet("/api/dbii/health"));

        Button tablasButton = crearBoton("Diccionario: tablas");
        tablasButton.setOnAction(event -> ejecutarGet("/api/dbii/diccionario/tablas"));

        Button procedimientosButton = crearBoton("Diccionario: procedimientos");
        procedimientosButton.setOnAction(event -> ejecutarGet("/api/dbii/diccionario/procedimientos"));

        Button objetosButton = crearBoton("Objetos Oracle");
        objetosButton.setOnAction(event -> ejecutarGet("/api/dbii/objetos"));

        HBox fila = new HBox(10, healthButton, tablasButton, procedimientosButton, objetosButton);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private HBox crearFilaAccionesReportes() {
        Button resumenButton = crearBoton("Vista resumen citas");
        resumenButton.setOnAction(event -> ejecutarGet("/api/dbii/resumen-citas"));

        Button estadosButton = crearBoton("Citas por estado");
        estadosButton.setOnAction(event -> ejecutarGet("/api/dbii/citas-por-estado"));

        Button procedimientoButton = crearBotonPrimario("Probar procedimiento INSERTAR_PACIENTE");
        procedimientoButton.setOnAction(event -> probarProcedimientoInsertarPaciente());

        HBox fila = new HBox(10, resumenButton, estadosButton, procedimientoButton);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private HBox crearFilaConsultaDocumento() {
        Button pacienteButton = crearBoton("Paciente por documento");
        pacienteButton.setOnAction(event -> consultarPacientePorDocumento());

        Button citasButton = crearBoton("Citas por documento");
        citasButton.setOnAction(event -> consultarCitasPorDocumento());

        Button auditoriaButton = crearBoton("Auditoria por documento");
        auditoriaButton.setOnAction(event -> consultarAuditoriaPorDocumento());

        Button evidenciaButton = crearBotonPrimario("Evidencia completa documento");
        evidenciaButton.setOnAction(event -> consultarEvidenciaCompletaPorDocumento());

        HBox fila = new HBox(10, pacienteButton, citasButton, auditoriaButton, evidenciaButton);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private Label crearLabelCampo(String texto) {
        Label label = new Label(texto);
        label.setStyle("""
                -fx-font-weight: bold;
                -fx-text-fill: #334155;
                """);
        return label;
    }

    private Button crearBoton(String texto) {
        Button button = new Button(texto);
        button.setStyle("""
                -fx-background-color: #ffffff;
                -fx-text-fill: #312e81;
                -fx-font-weight: bold;
                -fx-border-color: #c7d2fe;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                -fx-padding: 10 16 10 16;
                """);
        return button;
    }

    private Button crearBotonPrimario(String texto) {
        Button button = new Button(texto);
        button.setStyle("""
                -fx-background-color: #3730a3;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 10;
                -fx-padding: 10 16 10 16;
                """);
        return button;
    }

    private Button crearBotonSecundario(String texto) {
        Button button = new Button(texto);
        button.setStyle("""
                -fx-background-color: #e2e8f0;
                -fx-text-fill: #0f172a;
                -fx-font-weight: bold;
                -fx-background-radius: 10;
                -fx-padding: 10 16 10 16;
                """);
        return button;
    }

    private void autenticar() {
        try {
            String username = escaparJson(usernameField.getText());
            String password = escaparJson(passwordField.getText());

            String body = """
                    {
                      "username": "%s",
                      "password": "%s"
                    }
                    """.formatted(username, password);

            String respuesta = enviarPeticion("POST", "/api/auth/login", body, false);

            token = extraerAccessToken(respuesta);

            if (token == null || token.isBlank()) {
                estadoLabel.setText("Token no recibido");
                estadoLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
                mostrarSalida("ERROR AUTENTICANDO", respuesta);
                return;
            }

            estadoLabel.setText("Autenticado correctamente");
            estadoLabel.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
            mostrarSalida("LOGIN CORRECTO", respuesta);

        } catch (Exception ex) {
            estadoLabel.setText("Error de autenticacion");
            estadoLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
            mostrarError("Error autenticando contra agenda-service", ex);
        }
    }

    private void ejecutarGet(String endpoint) {
        if (!validarToken()) {
            return;
        }

        try {
            String respuesta = enviarPeticion("GET", endpoint, null, true);
            mostrarSalida("GET " + endpoint, respuesta);
        } catch (Exception ex) {
            mostrarError("Error consultando " + endpoint, ex);
        }
    }

    private void consultarPacientePorDocumento() {
        if (!validarToken()) {
            return;
        }

        String documento = obtenerDocumentoConsulta();

        if (documento.isBlank()) {
            mostrarSalida("DOCUMENTO REQUERIDO", "Debes ingresar el numero de documento.");
            return;
        }

        ejecutarGet("/api/dbii/pacientes/documento/" + codificarUrl(documento));
    }

    private void consultarCitasPorDocumento() {
        if (!validarToken()) {
            return;
        }

        String documento = obtenerDocumentoConsulta();

        if (documento.isBlank()) {
            mostrarSalida("DOCUMENTO REQUERIDO", "Debes ingresar el numero de documento.");
            return;
        }

        ejecutarGet("/api/dbii/citas/documento/" + codificarUrl(documento));
    }

    private void consultarAuditoriaPorDocumento() {
        if (!validarToken()) {
            return;
        }

        String documento = obtenerDocumentoConsulta();

        if (documento.isBlank()) {
            mostrarSalida("DOCUMENTO REQUERIDO", "Debes ingresar el numero de documento.");
            return;
        }

        ejecutarGet("/api/dbii/auditoria/documento/" + codificarUrl(documento));
    }

    private void consultarEvidenciaCompletaPorDocumento() {
        if (!validarToken()) {
            return;
        }

        String documento = obtenerDocumentoConsulta();

        if (documento.isBlank()) {
            mostrarSalida("DOCUMENTO REQUERIDO", "Debes ingresar el numero de documento.");
            return;
        }

        ejecutarGet("/api/dbii/evidencia/documento/" + codificarUrl(documento));
    }

    private String obtenerDocumentoConsulta() {
        if (documentoField == null || documentoField.getText() == null) {
            return "";
        }

        return documentoField.getText().trim();
    }

    private String codificarUrl(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private void probarProcedimientoInsertarPaciente() {
        if (!validarToken()) {
            return;
        }

        String documento = "999" + System.currentTimeMillis();

        String body = """
                {
                  "tipoDocumento": "CC",
                  "numDocumento": "%s",
                  "nombres": "Paciente",
                  "apellidos": "Procedimiento JavaFX",
                  "fechaNacimiento": "2000-01-01",
                  "celular": "3001234567",
                  "direccion": "Direccion prueba JavaFX DBII",
                  "email": "paciente.javafx.dbii@test.com"
                }
                """.formatted(documento);

        try {
            String respuesta = enviarPeticion(
                    "POST",
                    "/api/dbii/procedimientos/pacientes",
                    body,
                    true
            );

            mostrarSalida("POST /api/dbii/procedimientos/pacientes", respuesta);

        } catch (Exception ex) {
            mostrarError("Error ejecutando procedimiento almacenado Oracle", ex);
        }
    }

    private boolean validarToken() {
        if (token == null || token.isBlank()) {
            mostrarSalida(
                    "TOKEN REQUERIDO",
                    "Primero presiona el boton 'Autenticar admin'."
            );
            return false;
        }

        return true;
    }

    private String enviarPeticion(
            String metodo,
            String endpoint,
            String body,
            boolean usarToken
    ) throws Exception {
        String baseUrl = baseUrlField.getText();

        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://127.0.0.1:8081";
        }

        baseUrl = baseUrl.trim();

        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(metodo);
        connection.setRequestProperty("Accept", "application/json");

        if (usarToken) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }

        if (body != null) {
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(bodyBytes);
            }
        }

        int status = connection.getResponseCode();

        InputStream inputStream = status >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();

        String respuesta = leerRespuesta(inputStream);

        return "HTTP " + status + "\n\n" + formatearJsonBasico(respuesta);
    }

    private String leerRespuesta(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }

        StringBuilder respuesta = new StringBuilder();

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8)
                )
        ) {
            String linea;

            while ((linea = reader.readLine()) != null) {
                respuesta.append(linea);
            }
        }

        return respuesta.toString();
    }

    private String extraerAccessToken(String respuesta) {
        String marcador = "\"access_token\":\"";
        int inicio = respuesta.indexOf(marcador);

        if (inicio < 0) {
            return null;
        }

        inicio += marcador.length();

        int fin = respuesta.indexOf("\"", inicio);

        if (fin < 0) {
            return null;
        }

        return respuesta.substring(inicio, fin);
    }

    private String escaparJson(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String formatearJsonBasico(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }

        StringBuilder resultado = new StringBuilder();
        int nivel = 0;
        boolean dentroTexto = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                dentroTexto = !dentroTexto;
            }

            if (!dentroTexto && (c == '{' || c == '[')) {
                resultado.append(c).append('\n');
                nivel++;
                agregarIndentacion(resultado, nivel);
            } else if (!dentroTexto && (c == '}' || c == ']')) {
                resultado.append('\n');
                nivel--;
                agregarIndentacion(resultado, Math.max(nivel, 0));
                resultado.append(c);
            } else if (!dentroTexto && c == ',') {
                resultado.append(c).append('\n');
                agregarIndentacion(resultado, nivel);
            } else {
                resultado.append(c);
            }
        }

        return resultado.toString();
    }

    private void agregarIndentacion(StringBuilder builder, int nivel) {
        builder.append("  ".repeat(Math.max(0, nivel)));
    }

    private void mostrarSalida(String titulo, String contenido) {
        salidaArea.setText("""
                ============================================================
                %s
                ============================================================

                %s
                """.formatted(titulo, contenido));
    }

    private void mostrarError(String mensaje, Exception ex) {
        salidaArea.setText("""
                ============================================================
                ERROR
                ============================================================

                %s

                Detalle:
                %s
                """.formatted(mensaje, ex.getMessage()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}