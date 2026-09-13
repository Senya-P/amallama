package cz.cuni.mff.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

import cz.cuni.mff.core.Session;
import cz.cuni.mff.core.model.ModelManager;
import cz.cuni.mff.core.model.AppConfig;
import cz.cuni.mff.core.runtime.LlamafileRuntimeManager;
import cz.cuni.mff.core.runtime.RuntimeConfig;
import cz.cuni.mff.ui.controller.ChatController;
import cz.cuni.mff.ui.controller.ModelController;

/**
 * JavaFX entry point. Loads the chat view and wires it to a {@link Session}.
 */
public class App extends Application {

    private static final String TITLE = "amallama";
    @Override
    public void start(Stage stage) throws IOException {
        //String binary = getParameters().getRaw().stream().findFirst().orElse(null);

        AppConfig store = new AppConfig();
        ModelManager model = new ModelManager(store);
        Session session = new Session(new LlamafileRuntimeManager());
        RuntimeConfig config = model.selectInitial();
        if (config == null) {
            throw new IllegalStateException("No model selected and no model with runtime found");
        }
        session.start(config);

        FXMLLoader chatLoader = new FXMLLoader(App.class.getResource("chat.fxml"));
        ChatController chatController = new ChatController(session);
        chatLoader.setController(chatController);
        Parent chatView = chatLoader.load();

        FXMLLoader modelLoader = new FXMLLoader(App.class.getResource("model.fxml"));
        ModelController modelController = new ModelController(store, model, session);
        modelController.setOnModelLoaded(chatController::clearHistory);
        modelLoader.setController(modelController);
        Parent modelView = modelLoader.load();

        TabPane tabs = new TabPane(new Tab("Chat", chatView), new Tab("Model", modelView));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabs, 1200, 800);
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
