module sicxesimulator {
    requires javafx.controls;
    requires javafx.fxml;
    opens sicxesimulator.view to javafx.fxml;
    exports sicxesimulator;
}