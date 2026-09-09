module cz.cuni.mff {
    requires javafx.controls;
    requires javafx.fxml;

    // FXML needs reflective access to controllers
    opens cz.cuni.mff.controller to javafx.fxml;

    exports cz.cuni.mff;
}
