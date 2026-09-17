package cz.cuni.mff.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;

import cz.cuni.mff.core.AppConfig;
import cz.cuni.mff.core.Session;
import cz.cuni.mff.core.download.HFModelDownloader;
import cz.cuni.mff.core.download.ModelDownloader;
import cz.cuni.mff.core.hw.OshiHardwareDetector;
import cz.cuni.mff.core.model.ModelManager;
import cz.cuni.mff.core.monitor.MonitoringAgent;
import cz.cuni.mff.core.monitor.RuntimeMonitor;
import cz.cuni.mff.core.runtime.HwAwareRuntimeConfigGenerator;
import cz.cuni.mff.core.runtime.LlamafileRuntimeManager;
import cz.cuni.mff.core.runtime.RuntimePlan;
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
        ModelManager model = new ModelManager(store, new HwAwareRuntimeConfigGenerator(new OshiHardwareDetector()));
        Session session = new Session(new LlamafileRuntimeManager());
        new MonitoringAgent(new RuntimeMonitor(session)).tryStart();
        RuntimePlan plan = model.selectInitial();
        if (plan != null) {
            session.start(plan);
        }

        FXMLLoader chatLoader = new FXMLLoader(App.class.getResource("chat.fxml"));
        ChatController chatController = new ChatController(session);
        chatLoader.setController(chatController);
        Parent chatView = chatLoader.load();

        FXMLLoader modelLoader = new FXMLLoader(App.class.getResource("model.fxml"));
        ModelDownloader downloader = new HFModelDownloader(store.modelsDirectory());
        ModelController modelController = new ModelController(store, model, session, downloader);
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
