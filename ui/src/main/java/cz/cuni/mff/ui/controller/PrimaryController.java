package cz.cuni.mff.ui.controller;

import java.io.IOException;
import javafx.fxml.FXML;

import cz.cuni.mff.ui.SceneManager;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        SceneManager.setRoot("secondary");
    }
}
