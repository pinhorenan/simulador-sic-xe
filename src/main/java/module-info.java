module sicxesimulator {
    requires javafx.controls;
    requires javafx.base;
    requires javafx.graphics;
    requires java.logging;

    opens sicxesimulator.application.view to javafx.fxml, javafx.controls, javafx.media;
    opens sicxesimulator.application.components.tables to javafx.controls, javafx.fxml, javafx.media;
    opens sicxesimulator.application.model to javafx.controls, javafx.fxml, javafx.media;
    opens sicxesimulator.application.model.records to javafx.controls, javafx.fxml, javafx.media;

    exports sicxesimulator.application.view;
    exports sicxesimulator.application.components.panels;
    exports sicxesimulator.application.components.buttons;
    exports sicxesimulator.application.components.tables;
    exports sicxesimulator.application.model;
    exports sicxesimulator.application.model.records;
    exports sicxesimulator.application.controller;
    exports sicxesimulator.linker;
    exports sicxesimulator.loader;
    exports sicxesimulator.assembler;
    exports sicxesimulator.macroprocessor;
    exports sicxesimulator.utils;
    exports sicxesimulator.models;
    exports sicxesimulator.machine;
    exports sicxesimulator.machine.cpu;

    opens sicxesimulator.application.components.buttons to javafx.controls, javafx.fxml, javafx.media;
}
