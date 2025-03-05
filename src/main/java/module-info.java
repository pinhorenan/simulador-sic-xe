module sicxesimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires jdk.unsupported.desktop;
    requires java.logging;

    exports sicxesimulator.simulator.view to javafx.graphics;
}