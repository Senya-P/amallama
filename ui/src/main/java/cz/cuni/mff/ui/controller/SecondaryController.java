package cz.cuni.mff.ui.controller;

import java.io.IOException;
import javafx.fxml.FXML;

import cz.cuni.mff.ui.SceneManager;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        SceneManager.setRoot("primary");
    }
}
