package cz.cuni.mff.controller;

import java.io.IOException;
import javafx.fxml.FXML;

import cz.cuni.mff.SceneManager;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        SceneManager.setRoot("secondary");
    }
}
