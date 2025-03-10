module sicxesimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.logging;
    requires java.desktop;

    opens sicxesimulator.simulator.view to javafx.fxml, javafx.controls, javafx.media;
    opens sicxesimulator.simulator.view.components to javafx.fxml, javafx.controls, javafx.media;

    exports sicxesimulator.simulator.view;
    exports sicxesimulator.simulator.view.components;
    exports sicxesimulator.simulator.model;
    exports sicxesimulator.simulator.controller;
    exports sicxesimulator.machine;
    exports sicxesimulator.linker;
    exports sicxesimulator.loader;
    exports sicxesimulator.assembler;
    exports sicxesimulator.macroprocessor;
    exports sicxesimulator.utils;
    exports sicxesimulator.models;
    exports sicxesimulator.simulator.view.records;
    opens sicxesimulator.simulator.view.records to javafx.controls, javafx.fxml, javafx.media;
    exports sicxesimulator.simulator.view.components.tables;
    opens sicxesimulator.simulator.view.components.tables to javafx.controls, javafx.fxml, javafx.media;
}
