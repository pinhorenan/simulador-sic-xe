module sicxesimulator {
    exports sicxesimulator.utils;
    exports sicxesimulator.hardware;
    exports sicxesimulator.hardware.cpu;
    exports sicxesimulator.hardware.data;
    exports sicxesimulator.software.data.records;
    exports sicxesimulator.software.linker;
    exports sicxesimulator.software.assembler;
    exports sicxesimulator.software.assembler.data;
    exports sicxesimulator.software.assembler.util;
    exports sicxesimulator.software.macroprocessor;
    exports sicxesimulator.simulation.model;
    exports sicxesimulator.simulation.data;
    exports sicxesimulator.simulation.data.records;
    exports sicxesimulator.simulation.controller;
    exports sicxesimulator.simulation.components.tables;
    exports sicxesimulator.simulation.components.buttons;
    exports sicxesimulator.simulation.components.panels;
    exports sicxesimulator.simulation.interfaces;
    exports sicxesimulator.simulation.view to javafx.graphics;

    opens sicxesimulator.simulation.data to javafx.base;

    requires java.logging;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
    requires javafx.media;
}
