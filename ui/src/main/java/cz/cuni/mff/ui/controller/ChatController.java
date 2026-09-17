package cz.cuni.mff.ui.controller;

import cz.cuni.mff.core.Session;
import cz.cuni.mff.core.runtime.RuntimeStatus;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controller for the chat view. Adapts {@link Session} events to the FX thread
 * and renders the conversation history.
 */
public class ChatController {
    private static final int MAX_ERROR_RETRIES = 10;
    private final Session session;
    private final BooleanProperty busy = new SimpleBooleanProperty(true);
    private String statusStyle;

    @FXML private Region statusDot;
    @FXML private Label statusLabel;
    @FXML private Label modelLabel;
    @FXML private ScrollPane historyScroll;
    @FXML private VBox historyBox;
    @FXML private TextArea inputArea;
    @FXML private Button sendButton;

    /**
     * @param session the backend session this view talks to
     */
    public ChatController(Session session) {
        this.session = session;
    }

    @FXML
    private void initialize() {
        modelLabel.setText(session.modelName());

        setStatus(session.status());
        session.addListener(status -> Platform.runLater(() -> setStatus(status)));

        sendButton.disableProperty().bind(inputArea.textProperty().isEmpty().or(busy));

        inputArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown() && !busy.get()) {
                event.consume();
                onSend();
            }
        });
    }

    private void setStatus(RuntimeStatus status) {
        statusLabel.setText(status.name());
        if (statusStyle != null) {
            statusDot.getStyleClass().remove(statusStyle);
        }
        statusStyle = status.name().toLowerCase();
        statusDot.getStyleClass().add(statusStyle);

        busy.set(status != RuntimeStatus.RUNNING);
        if (status == RuntimeStatus.RUNNING) {
            modelLabel.setText(session.modelName());
        } else if (status == RuntimeStatus.FAILED) {
            showRuntimeError(0);
        }
    }

    /**
     * Appends the failure reason to the status label.
     */
    private void showRuntimeError(int attempt) {
        if (attempt >= MAX_ERROR_RETRIES) {
            return;
        }
        String error = session.lastError();
        if (error == null) {
            Platform.runLater(() -> showRuntimeError(attempt + 1));
            return;
        }
        statusLabel.setText("FAILED — " + error);
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

    /**
     * Clears the visible conversation history.
     */
    public void clearHistory() {
        historyBox.getChildren().clear();
    }

}