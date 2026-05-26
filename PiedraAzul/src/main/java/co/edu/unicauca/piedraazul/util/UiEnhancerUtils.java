package co.edu.unicauca.piedraazul.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class UiEnhancerUtils {

    private UiEnhancerUtils() {
    }

    public static void aplicarMejoras(Node root) {
        if (root == null) {
            return;
        }

        configurarDatePickers(root);
        acomodarBotonesLaterales(root);
    }

    private static void configurarDatePickers(Node node) {
        if (node instanceof DatePicker datePicker) {
            DatePickerUtils.configurarDatePicker(datePicker);
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                configurarDatePickers(child);
            }
        }
    }

    private static void acomodarBotonesLaterales(Node node) {
        if (node instanceof VBox vbox) {
            if (esSidebar(vbox) || contieneBotonesDeNavegacion(vbox)) {
                reducirEspaciadores(vbox);
                mejorarBotonesDeNavegacion(vbox);
            }
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                acomodarBotonesLaterales(child);
            }
        }
    }

    private static boolean esSidebar(VBox vbox) {
        return vbox.getStyleClass() != null
                && vbox.getStyleClass().contains("sidebar");
    }

    private static boolean contieneBotonesDeNavegacion(VBox vbox) {
        return vbox.getChildren()
                .stream()
                .anyMatch(UiEnhancerUtils::esBotonDeNavegacion);
    }

    private static boolean esBotonDeNavegacion(Node node) {
        if (!(node instanceof Button button)) {
            return false;
        }

        String texto = button.getText();

        if (texto == null) {
            return false;
        }

        String normalizado = texto.trim().toLowerCase();

        return normalizado.equals("cerrar sesión")
                || normalizado.equals("cerrar sesion")
                || normalizado.equals("volver")
                || normalizado.equals("volver al login");
    }

    private static void reducirEspaciadores(VBox vbox) {
        for (Node child : vbox.getChildren()) {
            Priority prioridad = VBox.getVgrow(child);

            if (Priority.ALWAYS.equals(prioridad)) {
                VBox.setVgrow(child, Priority.NEVER);
            }

            if (child instanceof Region region && !esBotonDeNavegacion(child)) {
                if (Priority.ALWAYS.equals(prioridad)
                        || region.getPrefHeight() > 100
                        || region.getMaxHeight() == Double.MAX_VALUE) {

                    region.setMinHeight(18);
                    region.setPrefHeight(26);
                    region.setMaxHeight(34);
                }
            }
        }
    }

    private static void mejorarBotonesDeNavegacion(VBox vbox) {
        for (Node child : vbox.getChildren()) {
            if (esBotonDeNavegacion(child) && child instanceof Button button) {
                button.setMaxWidth(Double.MAX_VALUE);
                button.setPrefHeight(42);

                if (!button.getStyleClass().contains("sidebar-action-button")) {
                    button.getStyleClass().add("sidebar-action-button");
                }
            }
        }

        vbox.setSpacing(Math.min(Math.max(vbox.getSpacing(), 10), 16));
    }
}