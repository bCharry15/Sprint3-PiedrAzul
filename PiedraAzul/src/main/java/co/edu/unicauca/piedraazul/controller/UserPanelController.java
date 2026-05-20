package co.edu.unicauca.piedraazul.controller;

import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.Vista;
import javafx.fxml.FXML;
import org.springframework.stereotype.Component;

@Component
public class UserPanelController {

    private final SceneManager sceneManager;

    public UserPanelController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    private void logout() {
        sceneManager.switchScene(Vista.LOGIN);
    }
}