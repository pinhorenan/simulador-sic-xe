module sicxesimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.logging;
    requires java.desktop;

    opens sicxesimulator.application.view to javafx.fxml, javafx.controls, javafx.media;
    opens sicxesimulator.application.view.components to javafx.fxml, javafx.controls, javafx.media;

    exports sicxesimulator.application.view;
    exports sicxesimulator.application.view.components;
    exports sicxesimulator.application.model;
    exports sicxesimulator.application.controller;
    exports sicxesimulator.machine;
    exports sicxesimulator.linker;
    exports sicxesimulator.loader;
    exports sicxesimulator.assembler;
    exports sicxesimulator.macroprocessor;
    exports sicxesimulator.utils;
    exports sicxesimulator.models;
    exports sicxesimulator.machine.cpu;
    exports sicxesimulator.application.model.records;
    opens sicxesimulator.application.model.records to javafx.controls, javafx.fxml, javafx.media;
    exports sicxesimulator.application.view.components.tables;
    opens sicxesimulator.application.view.components.tables to javafx.controls, javafx.fxml, javafx.media;
    opens sicxesimulator.application.model to javafx.controls, javafx.fxml, javafx.media;
}
