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
import cz.cuni.mff.ui.controller.ChatController;

/**
 * JavaFX entry point. Loads the chat view and wires it to a {@link Session}.
 */
public class App extends Application {

    private static final String TITLE = "amallama";
    @Override
    public void start(Stage stage) throws IOException {
        String binary = getParameters().getRaw().stream().findFirst().orElseThrow(() -> new IllegalArgumentException());

        Session session = new Session(new LlamafileRuntimeManager());
        session.start(RuntimeConfig.of(Path.of(binary)));

        FXMLLoader loader = new FXMLLoader(App.class.getResource("chat.fxml"));
        loader.setController(new ChatController(session));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(App.class.getResource("dark.css").toExternalForm());
        stage.setTitle(TITLE);
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
