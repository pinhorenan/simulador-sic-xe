module sicxesimulator.ui {
    requires javafx.controls;
    requires javafx.base;
    requires javafx.graphics;
    requires sicxesimulator.common;
    requires sicxesimulator.hardware;
    requires sicxesimulator.software;
    exports sicxesimulator.ui.model;
    exports sicxesimulator.ui.controller;
    exports sicxesimulator.ui.view;
    exports sicxesimulator.ui.components.tables;
    exports sicxesimulator.ui.components.buttons;
    exports sicxesimulator.ui.components.panels;

    opens sicxesimulator.ui.data to javafx.base;
}
