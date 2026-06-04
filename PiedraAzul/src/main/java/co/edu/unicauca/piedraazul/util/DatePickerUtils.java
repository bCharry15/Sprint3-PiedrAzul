package co.edu.unicauca.piedraazul.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.scene.control.Alert;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

public final class DatePickerUtils {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        datePicker.setShowWeekNumbers(false);

        configurarConversor(datePicker);
        configurarEntradaManual(datePicker);
        configurarComportamientoVisual(datePicker);
        configurarCalendario(datePicker);
    }

    private static void configurarConversor(DatePicker datePicker) {
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

        datePicker.getEditor().focusedProperty().addListener((obs, estabaEnFoco, tieneFoco) -> {
            if (!tieneFoco) {
                normalizarFechaDigitada(datePicker);
            }
        });

        datePicker.getEditor().setOnAction(event -> normalizarFechaDigitada(datePicker));
    }

    private static void configurarComportamientoVisual(DatePicker datePicker) {
        /*
         * Esto corrige el problema de que a veces la fecha se selecciona,
         * pero visualmente no aparece en el editor de inmediato.
         */
        datePicker.valueProperty().addListener((obs, valorAnterior, nuevaFecha) -> {
            if (datePicker.getEditor() == null) {
                return;
            }

            if (nuevaFecha == null) {
                datePicker.getEditor().clear();
            } else {
                datePicker.getEditor().setText(FORMATO_FECHA.format(nuevaFecha));
            }
        });

        datePicker.setOnHidden(event -> {
            if (datePicker.getValue() != null && datePicker.getEditor() != null) {
                datePicker.getEditor().setText(FORMATO_FECHA.format(datePicker.getValue()));
            }
        });
    }

    private static void configurarCalendario(DatePicker datePicker) {
        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);

                if (empty || fecha == null) {
                    setText(null);
                    setDisable(false);
                    return;
                }

                /*
                 * Si luego quieres bloquear fechas futuras o muy antiguas,
                 * aquí es el punto correcto para hacerlo.
                 * Por ahora solo dejamos el calendario libre.
                 */
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
            datePicker.getEditor().clear();
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

    private static void mostrarAlertaFechaInvalida() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Fecha inválida");
        alert.setHeaderText(null);
        alert.setContentText("Ingrese la fecha con el formato dd/MM/yyyy. Ejemplo: 10/01/1990.");
        alert.showAndWait();
    }
}