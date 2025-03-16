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
    exports sicxesimulator.application.controller;
    exports sicxesimulator.application.util;
    exports sicxesimulator.application.model;
    exports sicxesimulator.application.model.records;
    exports sicxesimulator.application.components.panels;
    exports sicxesimulator.application.components.buttons;
    exports sicxesimulator.application.components.tables;
    exports sicxesimulator.software.linker;
    exports sicxesimulator.software.loader;
    exports sicxesimulator.software.assembler;
    exports sicxesimulator.software.assembler.util;
    exports sicxesimulator.software.assembler.data;
    exports sicxesimulator.software.macroprocessor;
    exports sicxesimulator.utils;
    exports sicxesimulator.data;
    exports sicxesimulator.data.records;
    exports sicxesimulator.hardware;
    exports sicxesimulator.hardware.cpu;
    exports sicxesimulator.hardware.data;

    opens sicxesimulator.application.components.buttons to javafx.controls, javafx.fxml, javafx.media;
}
