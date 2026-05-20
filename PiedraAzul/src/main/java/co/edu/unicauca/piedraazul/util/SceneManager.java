package co.edu.unicauca.piedraazul.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class SceneManager {

    private static final Logger log = LoggerFactory.getLogger(SceneManager.class);
    private static final String FXML_BASE = "/fxml/";
    private static final String CSS_PATH  = "/styles/app.css";

    private final ApplicationContext applicationContext;
    private Stage primaryStage;

    public SceneManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Cambia la escena activa usando un valor del enum Vista.
     * Ejemplo: sceneManager.switchScene(Vista.ADMIN_PANEL);
     */
    public void switchScene(Vista vista) {
        switchScene(vista.getRuta());
    }

    /**
     * Cambia la escena activa usando una ruta relativa a /fxml/.
     * Se mantiene por compatibilidad interna.
     */
    private void switchScene(String rutaRelativa) {
        if (primaryStage == null) {
            throw new IllegalStateException(
                    "PrimaryStage no ha sido inicializado en SceneManager.");
        }

        try {
            URL fxmlLocation = getClass().getResource(FXML_BASE + rutaRelativa);

            if (fxmlLocation == null) {
                throw new IllegalArgumentException(
                        "No se encontró el archivo FXML: " + FXML_BASE + rutaRelativa);
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();

            Scene newScene;
            if (primaryStage.getScene() == null) {
                newScene = new Scene(root);
            } else {
                double width  = primaryStage.getWidth();
                double height = primaryStage.getHeight();
                newScene = (width > 0 && height > 0)
                        ? new Scene(root, width, height)
                        : new Scene(root);
            }

            URL cssLocation = getClass().getResource(CSS_PATH);
            if (cssLocation != null) {
                newScene.getStylesheets().add(cssLocation.toExternalForm());
            }

            primaryStage.setScene(newScene);
            primaryStage.show();

            log.debug("Escena cargada: {}", rutaRelativa);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al cargar la escena: " + rutaRelativa, e);
        }
    }
}
