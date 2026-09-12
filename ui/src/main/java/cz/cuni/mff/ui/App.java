package cz.cuni.mff.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

import cz.cuni.mff.core.Session;
import cz.cuni.mff.core.runtime.LlamafileRuntimeManager;
import cz.cuni.mff.core.runtime.RuntimeConfig;
import cz.cuni.mff.ui.controller.SessionController;

/**
 * JavaFX App — launch only. Scene management is handled by {@link SceneManager}.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        String binary = getParameters().getRaw().stream().findFirst().orElseThrow(() -> new IllegalArgumentException());

        Session session = new Session(new LlamafileRuntimeManager());
        session.start(RuntimeConfig.of(Path.of(binary)));

        FXMLLoader loader = new FXMLLoader(App.class.getResource("session.fxml"));
        loader.setController(new SessionController(session));
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 650);
        scene.getStylesheets().add(App.class.getResource("dark.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
