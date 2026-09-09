package cz.cuni.mff.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

/**
 * Manages scene navigation.
 */
public class SceneManager {

    private static Scene scene;

    static void bind(Scene scene) {
        SceneManager.scene = scene;
    }

    /**
     * Replaces the root of the current scene with the view loaded from the given FXML file.
     *
     * @param fxml the FXML name without the .fxml extension
     */
    public static void setRoot(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxml + ".fxml"));
        scene.setRoot(loader.load());
    }
}
