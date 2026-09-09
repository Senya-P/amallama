package cz.cuni.mff;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App — launch only. Scene management is handled by {@link SceneManager}.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(SceneManager.class.getResource("primary.fxml"));
        Scene scene = new Scene(root, 640, 480);
        SceneManager.bind(scene);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
