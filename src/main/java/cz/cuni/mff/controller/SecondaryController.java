package cz.cuni.mff.controller;

import java.io.IOException;
import javafx.fxml.FXML;

import cz.cuni.mff.SceneManager;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        SceneManager.setRoot("primary");
    }
}
