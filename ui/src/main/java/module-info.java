module amallama.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires amallama.core;

    // FXML needs reflective access to controllers
    opens cz.cuni.mff.ui.controller to javafx.fxml;

    exports cz.cuni.mff.ui;
}