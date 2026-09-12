package cz.cuni.mff.ui.controller;

import cz.cuni.mff.core.Session;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class SessionController {
    private final Session session;
    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    @FXML private Label statusLabel;
    @FXML private ScrollPane historyScroll;
    @FXML private VBox historyBox;
    @FXML private TextArea inputArea;
    @FXML private Button sendButton;

    public SessionController(Session session) {
        this.session = session;
    }
    @FXML
    private void initialize() {
        session.addListener(status -> Platform.runLater(() ->statusLabel.setText("Runtime: " + status)));
        statusLabel.setText("Runtime: " + session.status());

        sendButton.disableProperty().bind(inputArea.textProperty().isEmpty().or(busy));
    }

    @FXML
    private void onSend() {
        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        appendMessage("user", message);
        inputArea.clear();

        busy.set(true);
        session.send(message).whenComplete((response, error) -> Platform.runLater(() -> {
            busy.set(false);
            if (error != null) {
                appendMessage("error", session.lastError());
            } else {
                appendMessage("assistant", response.content());
            }
        }));
    }

    private void appendMessage(String role, String content) {
        Label message = new Label(content);
        message.getStyleClass().addAll("message", role);
        message.setWrapText(true);
        historyBox.getChildren().add(message);
        Platform.runLater(() -> historyScroll.setVvalue(1.0));
    }
}
