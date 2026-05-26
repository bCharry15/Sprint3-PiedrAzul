package co.edu.unicauca.piedraazul.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.IntStream;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

public final class DatePickerUtils {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final int ANIO_MINIMO = 1900;
    private static final int ANIO_MAXIMO = 2035;

    private DatePickerUtils() {
    }

    public static void configurarDatePicker(DatePicker datePicker) {
        if (datePicker == null) {
            return;
        }

        if (Boolean.TRUE.equals(datePicker.getProperties().get("piedraazul-datepicker-configurado"))) {
            return;
        }

        datePicker.getProperties().put("piedraazul-datepicker-configurado", true);

        datePicker.setEditable(true);
        datePicker.setPromptText("dd/MM/yyyy");

        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate fecha) {
                if (fecha == null) {
                    return "";
                }

                return FORMATO_FECHA.format(fecha);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.trim().isEmpty()) {
                    return null;
                }

                try {
                    return LocalDate.parse(texto.trim(), FORMATO_FECHA);
                } catch (DateTimeParseException ex) {
                    return null;
                }
            }
        });

        configurarEntradaManual(datePicker);
        bloquearCalendarioNativo(datePicker);
        instalarBotonSelectorPersonalizado(datePicker);
    }

    private static void configurarEntradaManual(DatePicker datePicker) {
        if (datePicker.getEditor() == null) {
            return;
        }

        datePicker.getEditor().setPromptText("dd/MM/yyyy");

        datePicker.getEditor().setTextFormatter(new TextFormatter<>(change -> {
            String nuevoTexto = change.getControlNewText();

            if (nuevoTexto == null || nuevoTexto.isEmpty()) {
                return change;
            }

            if (nuevoTexto.length() > 10) {
                return null;
            }

            if (!nuevoTexto.matches("[0-9/]*")) {
                return null;
            }

            return change;
        }));

        datePicker.focusedProperty().addListener((observable, estabaEnFoco, estaEnFoco) -> {
            if (!estaEnFoco) {
                normalizarFechaDigitada(datePicker);
            }
        });
    }

    private static void bloquearCalendarioNativo(DatePicker datePicker) {
        datePicker.setOnShowing(event -> {
            event.consume();
            datePicker.hide();

            Platform.runLater(() -> mostrarSelectorFecha(datePicker));
        });
    }

    private static void instalarBotonSelectorPersonalizado(DatePicker datePicker) {
        Platform.runLater(() -> {
            Node arrowButton = datePicker.lookup(".arrow-button");

            if (arrowButton != null) {
                arrowButton.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                    if (datePicker.isDisabled()) {
                        return;
                    }

                    event.consume();
                    datePicker.hide();
                    mostrarSelectorFecha(datePicker);
                });

                arrowButton.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                    if (datePicker.isDisabled()) {
                        return;
                    }

                    event.consume();
                });
            }
        });
    }

    public static void normalizarFechaDigitada(DatePicker datePicker) {
        if (datePicker == null || datePicker.getEditor() == null) {
            return;
        }

        String texto = datePicker.getEditor().getText();

        if (texto == null || texto.trim().isEmpty()) {
            datePicker.setValue(null);
            return;
        }

        try {
            LocalDate fecha = LocalDate.parse(texto.trim(), FORMATO_FECHA);
            datePicker.setValue(fecha);
            datePicker.getEditor().setText(FORMATO_FECHA.format(fecha));
        } catch (DateTimeParseException ex) {
            datePicker.setValue(null);
            datePicker.getEditor().clear();
            mostrarAlertaFechaInvalida();
        }
    }

    private static void mostrarSelectorFecha(DatePicker datePicker) {
        LocalDate fechaBase = obtenerFechaBase(datePicker);

        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar fecha");
        dialog.setHeaderText("Seleccione día, mes y año");

        ButtonType aceptarButtonType = new ButtonType("Aceptar", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(aceptarButtonType, ButtonType.CANCEL);

        ComboBox<Integer> diaCombo = new ComboBox<>();
        ComboBox<MesItem> mesCombo = new ComboBox<>();
        ComboBox<Integer> anioCombo = new ComboBox<>();

        diaCombo.setMaxWidth(Double.MAX_VALUE);
        mesCombo.setMaxWidth(Double.MAX_VALUE);
        anioCombo.setMaxWidth(Double.MAX_VALUE);

        diaCombo.setPrefWidth(120);
        mesCombo.setPrefWidth(190);
        anioCombo.setPrefWidth(140);

        mesCombo.setItems(FXCollections.observableArrayList(
                new MesItem(1, "Enero"),
                new MesItem(2, "Febrero"),
                new MesItem(3, "Marzo"),
                new MesItem(4, "Abril"),
                new MesItem(5, "Mayo"),
                new MesItem(6, "Junio"),
                new MesItem(7, "Julio"),
                new MesItem(8, "Agosto"),
                new MesItem(9, "Septiembre"),
                new MesItem(10, "Octubre"),
                new MesItem(11, "Noviembre"),
                new MesItem(12, "Diciembre")
        ));

        anioCombo.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(ANIO_MINIMO, ANIO_MAXIMO)
                        .boxed()
                        .toList()
        ));

        anioCombo.setValue(fechaBase.getYear());
        mesCombo.setValue(buscarMesItem(mesCombo, fechaBase.getMonthValue()));

        actualizarDias(diaCombo, anioCombo.getValue(), mesCombo.getValue().numero());
        seleccionarDiaSeguro(diaCombo, fechaBase.getDayOfMonth());

        anioCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || mesCombo.getValue() == null) {
                return;
            }

            Integer diaAnterior = diaCombo.getValue();
            actualizarDias(diaCombo, newValue, mesCombo.getValue().numero());
            seleccionarDiaSeguro(diaCombo, diaAnterior);
        });

        mesCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || anioCombo.getValue() == null) {
                return;
            }

            Integer diaAnterior = diaCombo.getValue();
            actualizarDias(diaCombo, anioCombo.getValue(), newValue.numero());
            seleccionarDiaSeguro(diaCombo, diaAnterior);
        });

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        ColumnConstraints columnaDia = new ColumnConstraints();
        columnaDia.setPercentWidth(25);

        ColumnConstraints columnaMes = new ColumnConstraints();
        columnaMes.setPercentWidth(45);

        ColumnConstraints columnaAnio = new ColumnConstraints();
        columnaAnio.setPercentWidth(30);

        grid.getColumnConstraints().addAll(columnaDia, columnaMes, columnaAnio);

        grid.add(new Label("Día"), 0, 0);
        grid.add(new Label("Mes"), 1, 0);
        grid.add(new Label("Año"), 2, 0);

        grid.add(diaCombo, 0, 1);
        grid.add(mesCombo, 1, 1);
        grid.add(anioCombo, 2, 1);

        Label ayuda = new Label("También puede escribir manualmente la fecha en formato dd/MM/yyyy. Ejemplo: 10/01/1990.");
        ayuda.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        grid.add(ayuda, 0, 2, 3, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == aceptarButtonType) {
                Integer dia = diaCombo.getValue();
                MesItem mes = mesCombo.getValue();
                Integer anio = anioCombo.getValue();

                if (dia == null || mes == null || anio == null) {
                    return null;
                }

                return LocalDate.of(anio, mes.numero(), dia);
            }

            return null;
        });

        dialog.showAndWait().ifPresent(fechaSeleccionada -> {
            datePicker.setValue(fechaSeleccionada);

            if (datePicker.getEditor() != null) {
                datePicker.getEditor().setText(FORMATO_FECHA.format(fechaSeleccionada));
            }
        });
    }

    private static LocalDate obtenerFechaBase(DatePicker datePicker) {
        if (datePicker.getValue() != null) {
            return datePicker.getValue();
        }

        if (datePicker.getEditor() != null) {
            String texto = datePicker.getEditor().getText();

            if (texto != null && !texto.trim().isEmpty()) {
                try {
                    return LocalDate.parse(texto.trim(), FORMATO_FECHA);
                } catch (DateTimeParseException ignored) {
                    return LocalDate.now();
                }
            }
        }

        return LocalDate.now();
    }

    private static void actualizarDias(ComboBox<Integer> diaCombo, int anio, int mes) {
        int diasDelMes = YearMonth.of(anio, mes).lengthOfMonth();

        diaCombo.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(1, diasDelMes)
                        .boxed()
                        .toList()
        ));
    }

    private static void seleccionarDiaSeguro(ComboBox<Integer> diaCombo, Integer diaAnterior) {
        if (diaCombo.getItems().isEmpty()) {
            return;
        }

        if (diaAnterior == null) {
            diaCombo.getSelectionModel().selectFirst();
            return;
        }

        if (diaCombo.getItems().contains(diaAnterior)) {
            diaCombo.setValue(diaAnterior);
        } else {
            diaCombo.setValue(diaCombo.getItems().get(diaCombo.getItems().size() - 1));
        }
    }

    private static MesItem buscarMesItem(ComboBox<MesItem> mesCombo, int numeroMes) {
        return mesCombo.getItems()
                .stream()
                .filter(mes -> mes.numero() == numeroMes)
                .findFirst()
                .orElse(mesCombo.getItems().get(0));
    }

    private static void mostrarAlertaFechaInvalida() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Fecha inválida");
        alert.setHeaderText(null);
        alert.setContentText("Ingrese la fecha con el formato dd/MM/yyyy. Ejemplo: 10/01/1990.");
        alert.showAndWait();
    }

    private record MesItem(int numero, String nombre) {
        @Override
        public String toString() {
            return nombre;
        }
    }
}